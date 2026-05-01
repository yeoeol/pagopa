package com.commerce.pagopa.user.application.dto.response;

import com.commerce.pagopa.user.domain.model.User;

public record UserResponseDto(
        Long userId,
        String email,
        String nickname,
        String profileImage,
        String role
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage(),
                user.getRoleName()
        );
    }
}
