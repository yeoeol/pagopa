package com.commerce.pagopa.domain.search.repository;

import com.commerce.pagopa.domain.search.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<SearchHistory> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    void deleteByUserId(Long userId);

    void deleteBySessionId(String sessionId);
}