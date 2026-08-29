package com.commerce.pagopa.searchhistory.domain.repository;

import com.commerce.pagopa.searchhistory.domain.model.SearchHistory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SearchHistoryRepository {

    SearchHistory save(SearchHistory searchHistory);

    Optional<SearchHistory> findById(Long id);

    void deleteByIdAndUserId(Long searchHistoryId, Long userId);

    void deleteByIdAndSessionId(Long searchHistoryId, String sessionId);

    List<SearchHistory> findByUserIdOrderByLastSearchedAtDesc(Long userId);

    List<SearchHistory> findBySessionIdOrderByLastSearchedAtDesc(String sessionId);

    void deleteByUserId(Long userId);

    void deleteBySessionId(String sessionId);

    void upsertByUserId(
            Long userId,
            String keyword,
            Instant lastSearchedAt
    );

    void upsertBySessionId(
            String SessionId,
            String keyword,
            Instant lastSearchedAt
    );
}
