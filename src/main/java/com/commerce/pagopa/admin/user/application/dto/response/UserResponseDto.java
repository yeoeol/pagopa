package com.commerce.pagopa.admin.user.application.dto.response;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;

import java.time.LocalDateTime;

public record UserResponseDto(
        Long userId,
        String email,
        String nickname,
        String profileImage,
        Provider provider,
        String providerId,
        String role,
        UserStatus userStatus,
        LocalDateTime withdrawnAt,  // 탈퇴 일시
        LocalDateTime banEndDate    // 정지 종료일
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage(),
                user.getProvider(),
                user.getProviderId(),
                user.getRoleName(),
                user.getUserStatus(),
                user.getWithdrawnAt(),
                user.getBanEndDate()
        );
    }
}
