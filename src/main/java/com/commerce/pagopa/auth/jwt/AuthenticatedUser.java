package com.commerce.pagopa.auth.jwt;

import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.model.enums.RoleCode;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.userrole.domain.model.UserRole;

import java.util.Set;
import java.util.stream.Collectors;

public record AuthenticatedUser(
		Long userId,
		String email,
		Set<RoleCode> roleCodes
) {
	public static AuthenticatedUser from(User user) {
		return new AuthenticatedUser(
				user.getId(),
				user.getEmail(),
				user.getUserRoles().stream()
						.map(UserRole::getRole)
						.filter(Role::isEnabled)
						.map(Role::getCode)
						.collect(Collectors.toUnmodifiableSet())
		);
	}
}
