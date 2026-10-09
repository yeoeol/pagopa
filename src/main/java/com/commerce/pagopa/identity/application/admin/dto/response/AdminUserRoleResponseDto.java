package com.commerce.pagopa.identity.application.admin.dto.response;

import com.commerce.pagopa.identity.domain.Role;
import com.commerce.pagopa.identity.domain.RoleCode;
import com.commerce.pagopa.identity.domain.UserRole;

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
