package com.commerce.pagopa.identity.infrastructure.jwt;

import java.util.Set;
import java.util.stream.Collectors;

import com.commerce.pagopa.identity.domain.Role;
import com.commerce.pagopa.identity.domain.RoleCode;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.identity.domain.UserRole;

public record AuthenticatedUser(
        Long userId,
        String email,
        Set<RoleCode> roleCodes
) {
    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getUserRoles()
                        .stream()
                        .map(UserRole::getRole)
                        .filter(Role::isEnabled)
                        .map(Role::getCode)
                        .collect(Collectors.toUnmodifiableSet())
        );
    }
}
