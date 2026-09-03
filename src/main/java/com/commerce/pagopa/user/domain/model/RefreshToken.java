package com.commerce.pagopa.user.domain.model;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "refresh_token",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_refresh_token_user_id",
                    columnNames = {"user_id"}
            ),
            @UniqueConstraint(
                    name = "uq_refresh_token_token",
                    columnNames = {"token"}
            )
        }
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "refresh_token_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_refresh_token_user")
    )
    private User user;

    @ToString.Include
    @Column(name = "token", length = 255, nullable = false)
    private String token;

    @Builder
    public RefreshToken(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public static RefreshToken create(User user, String token) {
        return RefreshToken.builder()
                .user(user)
                .token(token)
                .build();
    }

    public void updateToken(String newToken) {
        this.token = newToken;
    }
}
