package com.commerce.pagopa.searchhistory.domain.model;

import jakarta.persistence.*;

import java.time.Instant;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "search_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_search_history_user_id_keyword",
                        columnNames = {"user_id", "keyword"}
                ),
                @UniqueConstraint(
                        name = "uq_search_history_session_id_keyword",
                        columnNames = {"session_id", "keyword"}
                ),
        }
)
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "search_history_id", nullable = false)
    private Long id;

    // 비로그인 사용자를 위해 null을 허용
    @ToString.Include
    @Column(name = "user_id", nullable = true)
    private Long userId;

    // 비로그인 사용자를 식별하기 위한 세션 ID 또는 디바이스 ID (쿠키, 로컬스토리지 기반)
    @ToString.Include
    @Column(name = "session_id", length = 255, nullable = true)
    private String sessionId;

    @ToString.Include
    @Column(name = "keyword", nullable = false, length = 100)
    private String keyword;

    // 검색어 중복 갱신을 위한 전용 시간 필드
    @ToString.Include
    @Column(name = "last_searched_at", nullable = false)
    private Instant lastSearchedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private SearchHistory(
            Long userId,
            String sessionId,
            String keyword,
            Instant lastSearchedAt
    ) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.keyword = keyword;
        this.lastSearchedAt = lastSearchedAt;
    }

    public static SearchHistory createForUser(
            Long userId,
            String keyword,
            Instant lastSearchedAt
    ) {
        return SearchHistory.builder()
                .userId(userId)
                .keyword(keyword)
                .lastSearchedAt(lastSearchedAt)
                .build();
    }

    public static SearchHistory createForGuest(
            String sessionId,
            String keyword,
            Instant lastSearchedAt
    ) {
        return SearchHistory.builder()
                .sessionId(sessionId)
                .keyword(keyword)
                .lastSearchedAt(lastSearchedAt)
                .build();
    }

    // 중복 검색 시 시간 갱신
    public void updateLastSearchedAt(Instant lastSearchedAt) {
        this.lastSearchedAt = lastSearchedAt;
    }
}
