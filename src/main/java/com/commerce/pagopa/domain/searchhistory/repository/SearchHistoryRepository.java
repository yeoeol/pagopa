package com.commerce.pagopa.domain.searchhistory.repository;

import com.commerce.pagopa.domain.searchhistory.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    
    // 특정 유저의 최신 검색 기록 순 조회 (lastSearchedAt 기준 정렬)
    List<SearchHistory> findByUserIdOrderByLastSearchedAtDesc(Long userId);
    
    // 특정 세션의 최신 검색 기록 순 조회 (lastSearchedAt 기준 정렬)
    List<SearchHistory> findBySessionIdOrderByLastSearchedAtDesc(String sessionId);
    
    // 중복 검색어 체크를 위한 메서드 (유저)
    Optional<SearchHistory> findByUserIdAndKeyword(Long userId, String keyword);
    
    // 중복 검색어 체크를 위한 메서드 (비로그인 세션)
    Optional<SearchHistory> findBySessionIdAndKeyword(String sessionId, String keyword);
    
    void deleteByUserId(Long userId);

    void deleteBySessionId(String sessionId);
}