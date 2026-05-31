package com.commerce.pagopa.review.application.dto.response;

import com.commerce.pagopa.user.domain.model.User;

public record ReviewAuthorResponseDto(
        Long userId,
        String nickname,
        String profileImage
) {
    public static ReviewAuthorResponseDto from(User user) {
        return new ReviewAuthorResponseDto(
                user.getId(),
                user.getNickname(),
                user.getProfileImage()
        );
    }
}
