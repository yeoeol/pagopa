package com.commerce.pagopa.user.application.admin.dto.response;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;

import java.time.Instant;

public record AdminUserDetailResponseDto(
        Long userId,
        Provider provider,
        String name,
        String email,
        String phoneNumber,
        String profileImageUrl,
        UserStatus status,
        Instant suspendedUntil,
        Instant withdrawnAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static AdminUserDetailResponseDto from(User user) {
        return new AdminUserDetailResponseDto(
                user.getId(),
                user.getProvider(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.getStatus(),
                user.getSuspendedUntil(),
                user.getWithdrawnAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
