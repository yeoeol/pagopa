package com.commerce.pagopa.user.application.admin.dto.request;

import com.commerce.pagopa.role.domain.model.enums.RoleCode;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

public record AdminUserSearchRequestDto(
        String keyword,

		UserStatus status,

		RoleCode roleCode,

		@PositiveOrZero(message = "{validation.min}")
		Integer page,

        @Min(value = 1, message = "{validation.min}")
		@Max(value = 100, message = "{validation.max}")
		Integer size
) {
}
