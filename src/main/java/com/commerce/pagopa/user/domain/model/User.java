package com.commerce.pagopa.user.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.Role;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 255)
    private String nickname;

    @Column(length = 512)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userStatus;

    private LocalDateTime withdrawnAt;  // 탈퇴 일시
    private LocalDateTime banEndDate;   // 정지 종료일

    @Builder
    public User(
            String email, String nickname, String profileImage,
            Provider provider, String providerId, Role role, UserStatus userStatus
    ) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
        this.userStatus = userStatus;
    }

    public static User create(
            String email, String nickname, String profileImage,
            Provider provider, String providerId, Role role
    ) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .profileImage(profileImage)
                .provider(provider)
                .providerId(providerId)
                .role(role)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    public void updateProfile(String nickname, String profileImage) {
        if (nickname != null && !nickname.isBlank()) this.nickname = nickname;
        if (profileImage != null && !profileImage.isBlank()) this.profileImage = profileImage;
    }

    public void ban(long banSeconds) {
        validateActiveUserStatus();
        this.userStatus = UserStatus.BANNED;
        this.banEndDate = getBanEndDate(banSeconds);
    }

    public void unban() {
        validateBannedUserStatus();
        this.userStatus = UserStatus.ACTIVE;
        this.banEndDate = null;
    }

    public void withdraw() {
        validateActiveUserStatus();
        this.userStatus = UserStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    public void updateSellerRole() {
        validateActiveUserStatus();
        this.role = Role.ROLE_SELLER;
    }

    public String getRoleName() {
        return role.name();
    }

    private void validateActiveUserStatus() {
        if (this.userStatus != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_NOT_ACTIVE);
        }
    }

    private void validateBannedUserStatus() {
        if (this.userStatus != UserStatus.BANNED) {
            throw new BusinessException(ErrorCode.USER_NOT_BANNED);
        }
    }

    private static LocalDateTime getBanEndDate(long banSeconds) {
        return LocalDateTime.now().plusSeconds(banSeconds);
    }
}
