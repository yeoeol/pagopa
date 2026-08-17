package com.commerce.pagopa.role.application.response;

import com.commerce.pagopa.role.domain.model.Role;

public record RoleResponseDto(
		Long roleId,
		RoleCodeResponseDto code,
		String description,
		boolean enabled
) {
	public static RoleResponseDto from(Role role) {
		return new RoleResponseDto(
				role.getId(),
				RoleCodeResponseDto.from(role.getCode()),
				role.getDescription(),
				role.isEnabled()
		);
	}
}
