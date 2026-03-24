package com.commerce.pagopa.domain.user.dto.request;

public record UserUpdateRequestDto(
        String nickname,
        String profileImage
) {
}
