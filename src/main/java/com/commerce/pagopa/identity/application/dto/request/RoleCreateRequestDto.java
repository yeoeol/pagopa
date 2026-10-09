package com.commerce.pagopa.identity.application.dto.request;

import com.commerce.pagopa.identity.domain.RoleCode;

public record RoleCreateRequestDto(
        RoleCode roleCode,
        String description
) {
}
