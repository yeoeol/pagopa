package com.commerce.pagopa.user.application.admin.dto.response;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;

import java.time.Instant;
import java.util.List;

public record AdminUserListItemResponseDto(
        Long userId,
        String name,
        String email,
        Provider provider,
        StatusResponseDto<UserStatus> status,
        List<AdminUserRoleResponseDto> roles,
        Instant statusChangedAt,
        Instant createdAt
) {
    public static AdminUserListItemResponseDto from(
            User user,
            List<AdminUserRoleResponseDto> roles
    ) {
        return new AdminUserListItemResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProvider(),
                StatusResponseDto.from(user.getStatus()),
                List.copyOf(roles),
                user.getStatusChangedAt(),
                user.getCreatedAt()
        );
    }
}
