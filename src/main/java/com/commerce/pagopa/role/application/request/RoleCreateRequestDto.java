package com.commerce.pagopa.role.application.request;

import com.commerce.pagopa.role.domain.model.enums.RoleCode;

public record RoleCreateRequestDto(
		RoleCode roleCode,
		String description
) {
}
