package com.commerce.pagopa.review.application.dto.response;

import com.commerce.pagopa.identity.domain.User;

public record ReviewAuthorResponseDto(
        Long userId,
        String nickname,
        String profileImage
) {
    public static ReviewAuthorResponseDto from(User user) {
        return new ReviewAuthorResponseDto(
                user.getId(),
                user.getName(),
                user.getProfileImageUrl()
        );
    }
}
