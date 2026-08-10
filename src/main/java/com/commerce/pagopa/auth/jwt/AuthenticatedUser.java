package com.commerce.pagopa.auth.jwt;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Role;

public record AuthenticatedUser(
		Long userId,
		String email,
		Role role
) {
	public static AuthenticatedUser from(User user) {
		return new AuthenticatedUser(
				user.getId(),
				user.getEmail(),
				user.getRole()
		);
	}
}
