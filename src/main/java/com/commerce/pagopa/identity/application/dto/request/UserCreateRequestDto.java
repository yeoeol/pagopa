package com.commerce.pagopa.identity.application.dto.request;

import com.commerce.pagopa.identity.domain.Provider;

public record UserCreateRequestDto(
        Provider provider,
        String providerId,
        String name,
        String email,
        String profileImageUrl
) {
}
