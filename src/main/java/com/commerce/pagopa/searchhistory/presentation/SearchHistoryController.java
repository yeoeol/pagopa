package com.commerce.pagopa.searchhistory.presentation;

import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.global.util.CookieUtil;
import com.commerce.pagopa.searchhistory.application.SearchHistoryService;
import com.commerce.pagopa.searchhistory.application.dto.response.SearchHistoryResponseDto;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "SEARCH HISTORY API", description = "검색 기록 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search-histories")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @Operation(summary = "검색 기록 조회", description = "검색 기록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchHistoryResponseDto>>> getHistories(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            HttpServletRequest request
    ) {
        String sessionId = CookieUtil.getSessionIdFromCookie(request);

        return ResponseEntity.ok(
                ApiResponse.ok(searchHistoryService.getHistories(userId, sessionId))
        );
    }

    @Operation(summary = "검색 기록 단건 삭제", description = "검색 기록을 단건 삭제합니다.")
    @DeleteMapping("/{searchHistoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("searchHistoryId") Long searchHistoryId,
            HttpServletRequest request
    ) {
        String sessionId = CookieUtil.getSessionIdFromCookie(request);

        searchHistoryService.delete(searchHistoryId, userId, sessionId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "검색 기록 전체 삭제", description = "검색 기록을 전체 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllHistories(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            HttpServletRequest request
    ) {
        String sessionId = CookieUtil.getSessionIdFromCookie(request);

        searchHistoryService.deleteAll(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
