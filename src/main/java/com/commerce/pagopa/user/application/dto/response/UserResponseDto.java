package com.commerce.pagopa.user.application.dto.response;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;

import java.time.Instant;

public record UserResponseDto(
        Long userId,
        Provider provider,
        String name,
        String email,
        String profileImageUrl,
        StatusResponseDto<UserStatus> status,
        Instant suspendedUntil,
        Instant withdrawnAt
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getProvider(),
                user.getName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                StatusResponseDto.from(user.getStatus()),
                user.getSuspendedUntil(),
                user.getWithdrawnAt()
        );
    }
}
