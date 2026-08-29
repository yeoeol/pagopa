package com.commerce.pagopa.searchhistory.application.dto.response;

import com.commerce.pagopa.searchhistory.domain.model.SearchHistory;

import java.time.Instant;

public record SearchHistoryResponseDto(
        Long searchHistoryId,
        Long userId,
        String sessionId,
        String keyword,
        Instant searchedAt
) {
    public static SearchHistoryResponseDto from(SearchHistory searchHistory) {
        return new SearchHistoryResponseDto(
                searchHistory.getId(),
                searchHistory.getUserId(),
                searchHistory.getSessionId(),
                searchHistory.getKeyword(),
                searchHistory.getLastSearchedAt() // 갱신된 시간을 반환
        );
    }
}
