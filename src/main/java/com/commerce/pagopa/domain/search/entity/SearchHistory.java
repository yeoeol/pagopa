package com.commerce.pagopa.domain.search.entity;

import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(nullable = false, length = 100)
    private String keyword;

    @Builder(access = AccessLevel.PRIVATE)
    private SearchHistory(User user, String sessionId, String keyword) {
        this.user = user;
        this.sessionId = sessionId;
        this.keyword = keyword;
    }

    public static SearchHistory createForUser(User user, String keyword) {
        return SearchHistory.builder()
                .user(user)
                .keyword(keyword)
                .build();
    }

    public static SearchHistory createForGuest(String sessionId, String keyword) {
        return SearchHistory.builder()
                .sessionId(sessionId)
                .keyword(keyword)
                .build();
    }
}