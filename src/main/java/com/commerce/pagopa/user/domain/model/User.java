package com.commerce.pagopa.user.domain.model;

import com.commerce.pagopa.global.entity.Address;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;
import com.commerce.pagopa.userrole.domain.model.UserRole;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private final Set<UserRole> userRoles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private UserStatus status;

    @Column(name = "status_changed_at", nullable = false)
    private Instant statusChangedAt;  // 탈퇴 일시

    @Builder(access = AccessLevel.PRIVATE)
    private User(
            Provider provider,
            String providerId,
            String name,
            String email,
            Address address,
            String phoneNumber,
            String profileImageUrl,
            UserStatus status,
            Instant statusChangedAt
    ) {
        this.provider = provider;
        this.providerId = providerId;
        this.name = name;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.status = status;
        this.statusChangedAt = statusChangedAt;
    }

    public static User create(
            Provider provider,
            String providerId,
            String name,
            String email,
            String profileImageUrl,
            Instant statusChangedAt
    ) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .name(name)
                .email(email)
                .profileImageUrl(profileImageUrl)
                .status(UserStatus.ACTIVE)
                .statusChangedAt(statusChangedAt)
                .build();
    }

    public void updateProfile(String name, String profileImage) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (profileImage != null && !profileImage.isBlank()) {
            this.profileImageUrl = profileImage;
        }
    }

    public void addUserRole(UserRole userRole) {
        this.userRoles.add(userRole);
        userRole.assignUser(this);
    }

    public void activate(Instant activatedAt) {
        if (this.status == UserStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.status = UserStatus.ACTIVE;
        this.statusChangedAt = activatedAt;
    }

    public void suspend(Instant suspendedAt) {
        if (this.status == UserStatus.WITHDRAWN
                || this.status != UserStatus.ACTIVE
        ) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.status = UserStatus.SUSPENDED;
        this.statusChangedAt = suspendedAt;
    }

    public void ban(Instant bannedAt) {
        if (this.status == UserStatus.WITHDRAWN || this.status == UserStatus.BANNED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.status = UserStatus.BANNED;
        this.statusChangedAt = bannedAt;
    }

    public void withdraw(Instant withdrawnAt) {
        if (this.status == UserStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.status = UserStatus.WITHDRAWN;
        this.statusChangedAt = withdrawnAt;
    }
}
