package com.commerce.pagopa.identity.application.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.identity.domain.Provider;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.identity.domain.UserStatus;

public record AdminUserDetailResponseDto(Long userId, Provider provider, String name, String email, String phoneNumber,
        String profileImageUrl, StatusResponseDto<UserStatus> status, List<AdminUserRoleResponseDto> roles,
        LocalDateTime statusChangedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static AdminUserDetailResponseDto from(User user, List<AdminUserRoleResponseDto> roles) {
        return new AdminUserDetailResponseDto(user.getId(), user.getProvider(), user.getName(), user.getEmail(),
                user.getPhoneNumber(), user.getProfileImageUrl(), StatusResponseDto.from(user.getStatus()),
                List.copyOf(roles), user.getStatusChangedAt(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
