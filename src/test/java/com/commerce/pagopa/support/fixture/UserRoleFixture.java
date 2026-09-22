package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.identity.domain.Role;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.identity.domain.UserRole;

public final class UserRoleFixture {
	private UserRoleFixture() {
    }

	public static UserRole aUserRole(User user, Role role) {
		return UserRole.create(
				user,
				role
		);
	}
}
