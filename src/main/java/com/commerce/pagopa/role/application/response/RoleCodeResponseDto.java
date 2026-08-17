package com.commerce.pagopa.role.application.response;

import com.commerce.pagopa.role.domain.model.enums.RoleCode;

public record RoleCodeResponseDto(
		RoleCode roleCode,
		String description
) {
	public static RoleCodeResponseDto from(RoleCode roleCode) {
		return new RoleCodeResponseDto(
				roleCode,
				roleCode.getDescription()
		);
	}
}
