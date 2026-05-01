package com.commerce.pagopa.searchhistory.presentation;

import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.global.util.CookieUtil;
import com.commerce.pagopa.searchhistory.application.SearchHistoryService;
import com.commerce.pagopa.searchhistory.application.dto.response.SearchHistoryResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("@searchHistoryOwnerValidator.isOwner(#searchHistoryId, #userDetails != null ? #userDetails.userId : null)")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
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
