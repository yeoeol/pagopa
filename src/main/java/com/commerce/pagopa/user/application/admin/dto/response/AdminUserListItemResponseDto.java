package com.commerce.pagopa.user.application.admin.dto.response;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;

import java.time.Instant;

public record AdminUserListItemResponseDto(
        Long userId,
        String name,
        String email,
        Provider provider,
        UserStatus status,
        Instant suspendedUntil,
        Instant createdAt
) {
    public static AdminUserListItemResponseDto from(User user) {
        return new AdminUserListItemResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProvider(),
                user.getStatus(),
                user.getSuspendedUntil(),
                user.getCreatedAt()
        );
    }
}
