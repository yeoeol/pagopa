package com.commerce.pagopa.user.application.admin.dto.response;

import com.commerce.pagopa.global.response.StatusResponseDto;
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
        StatusResponseDto<UserStatus> status,
        Instant statusChangedAt,
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
                StatusResponseDto.from(user.getStatus()),
                user.getStatusChangedAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
