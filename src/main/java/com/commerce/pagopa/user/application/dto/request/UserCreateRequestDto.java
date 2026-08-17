package com.commerce.pagopa.user.application.dto.request;

import com.commerce.pagopa.user.domain.model.enums.Provider;

public record UserCreateRequestDto(
		Provider provider,
		String providerId,
		String name,
		String email,
		String profileImageUrl
) {
}
