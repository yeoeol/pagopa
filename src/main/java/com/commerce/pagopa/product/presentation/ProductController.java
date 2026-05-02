package com.commerce.pagopa.product.presentation;

import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.cookie.GuestSessionCookieFactory;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.product.application.ProductService;
import com.commerce.pagopa.product.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.product.application.dto.response.ProductResponseDto;
import com.commerce.pagopa.searchhistory.application.SearchHistoryService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final GuestSessionCookieFactory guestSessionCookieFactory;

    /**
     * 상품 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.ok(productService.findAllWithActiveAndSoldOut())
        );
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getDetail(
            @PathVariable("id") Long productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(productService.find(productId))
        );
    }

    /**
     * 상품 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> search(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute ProductSearchCondition productSearchCondition,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String keyword = productSearchCondition.productName();

        if (keyword != null && !keyword.isBlank()) {
            if (userDetails != null) {
                searchHistoryService.saveHistory(userDetails.getUserId(), null, keyword);
            } else {
                String sessionId = guestSessionCookieFactory.getOrCreateGuestSessionId(request, response);
                searchHistoryService.saveHistory(null, sessionId, keyword);
            }
        }

        return ResponseEntity.ok(
                ApiResponse.ok(productService.search(productSearchCondition))
        );
    }

    /**
     * 카테고리별 상품 목록 조회
     */
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getProductsByCategory(
            @PathVariable("categoryId") Long categoryId,
            @PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(productService.findAllByCategory(categoryId, pageable))
        );
    }
}
