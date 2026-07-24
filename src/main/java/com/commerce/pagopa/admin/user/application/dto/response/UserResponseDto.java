package com.commerce.pagopa.admin.user.application.dto.response;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;

import java.time.Instant;

public record UserResponseDto(
        Long userId,
        String email,
        String nickname,
        String profileImage,
        Provider provider,
        String providerId,
        String role,
        UserStatus userStatus,
        Instant withdrawnAt,  // 탈퇴 일시
        Instant banEndDate    // 정지 종료일
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImage(),
                user.getProvider(),
                user.getProviderId(),
                user.getRoleName(),
                user.getUserStatus(),
                user.getWithdrawnAt(),
                user.getBannedUntil()
        );
    }
}
