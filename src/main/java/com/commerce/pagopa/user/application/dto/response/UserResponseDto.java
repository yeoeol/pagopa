package com.commerce.pagopa.user.application.dto.response;

import com.commerce.pagopa.user.domain.model.User;

public record UserResponseDto(
        Long userId,
        String email,
        String name,
        String profileImage,
        String role
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImageUrl(),
                user.getRoleName()
        );
    }
}
