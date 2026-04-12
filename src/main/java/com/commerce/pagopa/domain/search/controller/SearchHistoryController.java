package com.commerce.pagopa.domain.search.controller;

import com.commerce.pagopa.domain.search.dto.response.SearchHistoryResponseDto;
import com.commerce.pagopa.domain.search.service.SearchHistoryService;
import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search-histories")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    // 쿠키 이름 정의
    private static final String GUEST_SESSION_COOKIE = "GUEST_SESSION_ID";

    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchHistoryResponseDto>>> getHistories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        String sessionId = getSessionIdFromCookie(request);

        return ResponseEntity.ok(
                ApiResponse.ok(searchHistoryService.getHistories(userId, sessionId))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(
            @PathVariable("id") Long searchHistoryId
    ) {
        searchHistoryService.delete(searchHistoryId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllHistories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        String sessionId = getSessionIdFromCookie(request);

        searchHistoryService.deleteAll(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 쿠키에서 비로그인 사용자용 세션 ID를 추출
    private String getSessionIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (GUEST_SESSION_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}