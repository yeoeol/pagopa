package com.commerce.pagopa.identity.application.dto.response;

import com.commerce.pagopa.identity.domain.RoleCode;

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
