package com.commerce.pagopa.searchhistory.infrastructure.persistence;

import com.commerce.pagopa.searchhistory.domain.model.SearchHistory;
import com.commerce.pagopa.searchhistory.domain.repository.SearchHistoryRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SearchHistoryJpaRepository extends JpaRepository<SearchHistory, Long>, SearchHistoryRepository {

    @Override
    List<SearchHistory> findByUserIdOrderByLastSearchedAtDesc(Long userId);

    @Override
    List<SearchHistory> findBySessionIdOrderByLastSearchedAtDesc(String sessionId);

    @Override
    @Modifying
    @Query("DELETE FROM SearchHistory sh " +
            "WHERE sh.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Override
    @Modifying
    @Query("DELETE FROM SearchHistory sh " +
            "WHERE sh.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);

    @Override
    @Modifying
    @Query(value = """
            INSERT INTO search_history (
                user_id,
                keyword,
                last_searched_at
            )
            VALUES (
                :userId,
                :keyword,
                :lastSearchedAt
            )
            ON DUPLICATE KEY UPDATE
                last_searched_at = :lastSearchedAt
            """, nativeQuery = true)
    void upsertByUserId(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("lastSearchedAt") Instant lastSearchedAt
    );

    @Override
    @Modifying
    @Query(value = """
            INSERT INTO search_history (
                session_id,
                keyword,
                last_searched_at
            )
            VALUES (
                :sessionId,
                :keyword,
                :lastSearchedAt
            )
            ON DUPLICATE KEY UPDATE
                last_searched_at = :lastSearchedAt
            """, nativeQuery = true)
    void upsertBySessionId(
            @Param("sessionId") String sessionId,
            @Param("keyword") String keyword,
            @Param("lastSearchedAt") Instant lastSearchedAt
    );
}
