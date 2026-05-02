package com.commerce.pagopa.scrap.domain.model;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "scraps",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "target_type", "target_id"}
                )
        }
)
public class Scrap extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scrap_id")
    private Long id;

    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityType targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private Scrap(Long targetId, EntityType targetType, User user) {
        this.targetId = targetId;
        this.targetType = targetType;
        this.user = user;
    }

    public static Scrap create(Long targetId, EntityType targetType, User user) {
        return Scrap.builder()
                .targetId(targetId)
                .targetType(targetType)
                .user(user)
                .build();
    }
}
