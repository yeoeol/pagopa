package com.commerce.pagopa.domain.product.controller;

import com.commerce.pagopa.domain.product.dto.request.ProductSearch;
import com.commerce.pagopa.domain.product.dto.response.ProductResponseDto;
import com.commerce.pagopa.domain.product.service.ProductService;
import com.commerce.pagopa.domain.searchhistory.service.SearchHistoryService;
import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final SearchHistoryService searchHistoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.ok(productService.findAllWithActiveAndSoldOut())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(productService.find(id))
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> search(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute ProductSearch productSearch,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String keyword = productSearch.productName();
        
        // 검색어가 있을 때만 기록
        if (keyword != null && !keyword.isBlank()) {
            if (userDetails != null) {
                // 로그인 사용자 검색 기록 저장
                searchHistoryService.saveHistory(userDetails.getUserId(), null, keyword);
            } else {
                // 비로그인 사용자: 쿠키에서 세션 ID 확인, 없으면 발급
                String sessionId = CookieUtil.getOrCreateGuestSessionId(request, response);
                searchHistoryService.saveHistory(null, sessionId, keyword);
            }
        }

        return ResponseEntity.ok(
                ApiResponse.ok(productService.search(productSearch))
        );
    }
}
