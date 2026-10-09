package com.commerce.pagopa.identity.application.dto.response;

import com.commerce.pagopa.identity.domain.Role;

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
