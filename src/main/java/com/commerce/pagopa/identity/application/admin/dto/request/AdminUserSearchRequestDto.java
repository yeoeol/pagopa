package com.commerce.pagopa.identity.application.admin.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

import com.commerce.pagopa.identity.domain.RoleCode;
import com.commerce.pagopa.identity.domain.UserStatus;

public record AdminUserSearchRequestDto(String keyword, UserStatus status, RoleCode roleCode,
        @PositiveOrZero(message = "{validation.min}") Integer page,
        @Min(value = 1, message = "{validation.min}") @Max(value = 100, message = "{validation.max}") Integer size) {
}
