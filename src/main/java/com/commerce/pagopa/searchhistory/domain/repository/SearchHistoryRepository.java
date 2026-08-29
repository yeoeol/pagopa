package com.commerce.pagopa.searchhistory.domain.repository;

import com.commerce.pagopa.searchhistory.domain.model.SearchHistory;

import java.util.List;
import java.util.Optional;

public interface SearchHistoryRepository {

    SearchHistory save(SearchHistory searchHistory);

    Optional<SearchHistory> findById(Long id);

    void deleteByIdAndUserId(Long searchHistoryId, Long userId);

    void deleteByIdAndSessionId(Long searchHistoryId, String sessionId);

    List<SearchHistory> findByUserIdOrderByLastSearchedAtDesc(Long userId);

    List<SearchHistory> findBySessionIdOrderByLastSearchedAtDesc(String sessionId);

    Optional<SearchHistory> findByUserIdAndKeywordForUpdate(Long userId, String keyword);

    Optional<SearchHistory> findBySessionIdAndKeywordForUpdate(String sessionId, String keyword);

    void deleteByUserId(Long userId);

    void deleteBySessionId(String sessionId);
}
