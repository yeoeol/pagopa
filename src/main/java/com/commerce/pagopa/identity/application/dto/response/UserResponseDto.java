package com.commerce.pagopa.identity.application.dto.response;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.identity.domain.Provider;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.identity.domain.UserStatus;

import java.time.LocalDateTime;

public record UserResponseDto(
        Long userId,
        Provider provider,
        String name,
        String email,
        String profileImageUrl,
        StatusResponseDto<UserStatus> status,
        LocalDateTime statusChangedAt
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getProvider(),
                user.getName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                StatusResponseDto.from(user.getStatus()),
                user.getStatusChangedAt()
        );
    }
}
