package com.commerce.pagopa.discovery.application.dto.response;

import java.time.LocalDateTime;

import com.commerce.pagopa.discovery.domain.SearchHistory;

public record SearchHistoryResponseDto(Long searchHistoryId, Long userId, String sessionId, String keyword,
        LocalDateTime searchedAt) {
    public static SearchHistoryResponseDto from(SearchHistory searchHistory) {
        return new SearchHistoryResponseDto(searchHistory.getId(), searchHistory.getUserId(),
                searchHistory.getSessionId(), searchHistory.getKeyword(), searchHistory.getLastSearchedAt() // 갱신된 시간을
                                                                                                            // 반환
        );
    }
}
