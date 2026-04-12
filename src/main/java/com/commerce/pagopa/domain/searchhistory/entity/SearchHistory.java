package com.commerce.pagopa.domain.searchhistory.entity;

import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "search_histories")
public class SearchHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_history_id")
    private Long id;

    // 비로그인 사용자를 위해 null을 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 비로그인 사용자를 식별하기 위한 세션 ID 또는 디바이스 ID (쿠키, 로컬스토리지 기반)
    @Column(name = "session_id")
    private String sessionId;

    @Column(nullable = false, length = 100)
    private String keyword;

    // 검색어 중복 갱신을 위한 전용 시간 필드
    @Column(name = "last_searched_at", nullable = false)
    private LocalDateTime lastSearchedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private SearchHistory(User user, String sessionId, String keyword, LocalDateTime lastSearchedAt) {
        this.user = user;
        this.sessionId = sessionId;
        this.keyword = keyword;
        this.lastSearchedAt = lastSearchedAt;
    }

    public static SearchHistory createForUser(User user, String keyword) {
        return SearchHistory.builder()
                .user(user)
                .keyword(keyword)
                .lastSearchedAt(LocalDateTime.now())
                .build();
    }

    public static SearchHistory createForGuest(String sessionId, String keyword) {
        return SearchHistory.builder()
                .sessionId(sessionId)
                .keyword(keyword)
                .lastSearchedAt(LocalDateTime.now())
                .build();
    }

    // 중복 검색 시 시간 갱신
    public void updateLastSearchedAt() {
        this.lastSearchedAt = LocalDateTime.now();
    }
}