package com.commerce.pagopa.domain.product.controller;

import com.commerce.pagopa.domain.product.dto.request.ProductRegisterRequestDto;
import com.commerce.pagopa.domain.product.dto.request.ProductSearch;
import com.commerce.pagopa.domain.product.dto.response.ProductResponseDto;
import com.commerce.pagopa.domain.product.service.ProductService;
import com.commerce.pagopa.domain.search.service.SearchHistoryService;
import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final SearchHistoryService searchHistoryService;

    private static final String GUEST_SESSION_COOKIE = "GUEST_SESSION_ID";

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponseDto>> register(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProductRegisterRequestDto requestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(productService.register(userDetails.getUserId(), requestDto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.ok(productService.findAll())
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
            @ModelAttribute ProductSearch productSearch,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String keyword = productSearch.name();
        
        // 검색어가 있을 때만 기록
        if (keyword != null && !keyword.isBlank()) {
            if (userDetails != null) {
                // 로그인 사용자 검색 기록 저장
                searchHistoryService.saveHistory(userDetails.getUserId(), null, keyword);
            } else {
                // 비로그인 사용자: 쿠키에서 세션 ID 확인, 없으면 발급
                String sessionId = getOrCreateGuestSessionId(request, response);
                searchHistoryService.saveHistory(null, sessionId, keyword);
            }
        }

        return ResponseEntity.ok(
                ApiResponse.ok(productService.search(productSearch))
        );
    }

    private String getOrCreateGuestSessionId(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (GUEST_SESSION_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 쿠키가 없다면 새로 생성하여 응답에 추가
        String newSessionId = UUID.randomUUID().toString();
        Cookie cookie = new Cookie(GUEST_SESSION_COOKIE, newSessionId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24 * 30); // 30일 유지
        response.addCookie(cookie);

        return newSessionId;
    }
}