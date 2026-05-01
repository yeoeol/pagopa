package com.commerce.pagopa.searchhistory.infrastructure.persistence;

import com.commerce.pagopa.searchhistory.domain.model.SearchHistory;
import com.commerce.pagopa.searchhistory.domain.repository.SearchHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchHistoryJpaRepository extends JpaRepository<SearchHistory, Long>, SearchHistoryRepository {

    @Override
    List<SearchHistory> findByUserIdOrderByLastSearchedAtDesc(Long userId);

    @Override
    List<SearchHistory> findBySessionIdOrderByLastSearchedAtDesc(String sessionId);

    @Override
    Optional<SearchHistory> findByUserIdAndKeyword(Long userId, String keyword);

    @Override
    Optional<SearchHistory> findBySessionIdAndKeyword(String sessionId, String keyword);

    @Override
    void deleteByUserId(Long userId);

    @Override
    void deleteBySessionId(String sessionId);
}
