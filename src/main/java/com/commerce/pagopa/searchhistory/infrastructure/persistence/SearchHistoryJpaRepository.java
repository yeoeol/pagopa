package com.commerce.pagopa.searchhistory.infrastructure.persistence;

import com.commerce.pagopa.searchhistory.domain.model.SearchHistory;
import com.commerce.pagopa.searchhistory.domain.repository.SearchHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Modifying
    @Query("DELETE FROM SearchHistory sh " +
            "WHERE sh.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Override
    @Modifying
    @Query("DELETE FROM SearchHistory sh " +
            "WHERE sh.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);
}
