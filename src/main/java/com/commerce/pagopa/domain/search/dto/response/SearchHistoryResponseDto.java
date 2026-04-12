package com.commerce.pagopa.domain.search.dto.response;

import com.commerce.pagopa.domain.search.entity.SearchHistory;

import java.time.LocalDateTime;

public record SearchHistoryResponseDto(
        Long searchHistoryId,
        String keyword,
        LocalDateTime searchedAt
) {
    public static SearchHistoryResponseDto from(SearchHistory searchHistory) {
        return new SearchHistoryResponseDto(
                searchHistory.getId(),
                searchHistory.getKeyword(),
                searchHistory.getCreatedAt()
        );
    }
}