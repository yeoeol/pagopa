package com.commerce.pagopa.domain.search.controller;

import com.commerce.pagopa.domain.search.dto.response.SearchHistoryResponseDto;
import com.commerce.pagopa.domain.search.service.SearchHistoryService;
import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.global.util.CookieUtil;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchHistoryResponseDto>>> getHistories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        String sessionId = CookieUtil.getSessionIdFromCookie(request);

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
        String sessionId = CookieUtil.getSessionIdFromCookie(request);

        searchHistoryService.deleteAll(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
