package com.commerce.pagopa.identity.application.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.identity.domain.Provider;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.identity.domain.UserStatus;

public record AdminUserListItemResponseDto(
        Long userId,
        String name,
        String email,
        Provider provider,
        StatusResponseDto<UserStatus> status,
        List<AdminUserRoleResponseDto> roles,
        LocalDateTime statusChangedAt,
        LocalDateTime createdAt
) {
    public static AdminUserListItemResponseDto from(User user, List<AdminUserRoleResponseDto> roles) {
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
