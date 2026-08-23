package com.commerce.pagopa.user.application.admin.dto.response;

import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.model.enums.RoleCode;
import com.commerce.pagopa.userrole.domain.model.UserRole;

public record AdminUserRoleResponseDto(
		RoleCode code,
		String description,
		boolean enabled
) {
	public static AdminUserRoleResponseDto from(UserRole userRole) {
		Role role = userRole.getRole();

		return new AdminUserRoleResponseDto(
				role.getCode(),
				role.getDescription(),
				role.isEnabled()
		);
	}
}
