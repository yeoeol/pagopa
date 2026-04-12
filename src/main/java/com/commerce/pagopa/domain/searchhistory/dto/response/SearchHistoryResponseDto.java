package com.commerce.pagopa.domain.searchhistory.dto.response;

import com.commerce.pagopa.domain.searchhistory.entity.SearchHistory;

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
                searchHistory.getLastSearchedAt() // 갱신된 시간을 반환
        );
    }
}