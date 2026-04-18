package com.commerce.pagopa.domain.admin.user.dto.response;

import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.entity.enums.Provider;

public record UserResponseDto(
        Long userId,
        String email,
        String nickname,
        String profileImage,
        Provider provider,
        String providerId,
        String role
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage(),
                user.getProvider(),
                user.getProviderId(),
                user.getRole().name()
        );
    }
}
