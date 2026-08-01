package com.commerce.pagopa.user.domain.model;

import com.commerce.pagopa.global.entity.Address;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.Role;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;
import jakarta.persistence.*;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uq_user_provider_provider_id",
                        columnNames = {"provider", "provider_id"}
                )
        }
)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 50, nullable = false)
    private Provider provider;

    @Column(name = "provider_id", length = 255, nullable = false)
    private String providerId;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Embedded
    private Address address;

    @Column(name = "phone_number", length = 20, nullable = true)
    private String phoneNumber;

    @Column(name = "profile_image_url", length = 512, nullable = false)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private UserStatus status;

    @Column(name = "suspended_until", nullable = true)
    private Instant suspendedUntil;   // 정지 종료일

    @Column(name = "withdrawn_at", nullable = true)
    private Instant withdrawnAt;  // 탈퇴 일시

    @Builder
    public User(
            Provider provider,
            String providerId,
            String name,
            String email,
            String profileImageUrl,
            Role role,
            UserStatus status
    ) {
        this.provider = provider;
        this.providerId = providerId;
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.status = status;
    }

    public static User create(
            String email, String name, String profileImageUrl,
            Provider provider, String providerId, Role role
    ) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .name(name)
                .email(email)
                .profileImageUrl(profileImageUrl)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public void updateProfile(String name, String profileImage) {
        if (name != null && !name.isBlank()) this.name = name;
        if (profileImage != null && !profileImage.isBlank()) this.profileImageUrl = profileImage;
    }

    public void withdraw() {
        validateActiveUserStatus();
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = Instant.now();
    }

    public String getRoleName() {
        return role.name();
    }

    private void validateActiveUserStatus() {
        if (this.status != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_NOT_ACTIVE);
        }
    }
}
