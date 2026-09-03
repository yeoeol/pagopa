package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.userrole.domain.model.UserRole;

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
