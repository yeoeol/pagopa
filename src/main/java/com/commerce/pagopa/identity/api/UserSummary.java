package com.commerce.pagopa.identity.api;

public record UserSummary(
		Long userId,
		String name,
		String email
) {
}
