package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.model.enums.RoleCode;

public final class RoleFixture {
	public RoleFixture() {
	}

	public static Role aRoleUser() {
		return aRole(RoleCode.ROLE_USER, "회원입니다.");
	}

	public static Role aRoleSeller() {
		return aRole(RoleCode.ROLE_SELLER, "판매자입니다.");
	}

	public static Role aRoleAdmin() {
		return aRole(RoleCode.ROLE_ADMIN, "관리자입니다.");
	}

	private static Role aRole(RoleCode roleCode, String description) {
		return Role.create(roleCode, description);
	}
}
