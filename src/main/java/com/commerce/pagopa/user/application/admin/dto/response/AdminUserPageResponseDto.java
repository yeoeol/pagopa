package com.commerce.pagopa.user.application.admin.dto.response;

import com.commerce.pagopa.user.domain.model.User;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;

public record AdminUserPageResponseDto(
        List<AdminUserListItemResponseDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        int startPage,
        int endPage
) {
    private static final int PAGE_WINDOW_SIZE = 10;

    public static AdminUserPageResponseDto from(
            Page<User> users,
            Map<Long, List<AdminUserRoleResponseDto>> rolesByUserId
    ) {
        int totalPages = users.getTotalPages();
        int currentPage = users.getNumber();
        int startPage = max(0, currentPage - PAGE_WINDOW_SIZE / 2);
        int endPage = min(
                max(totalPages - 1, 0),
                startPage + PAGE_WINDOW_SIZE - 1
        );
        startPage = max(0, endPage - PAGE_WINDOW_SIZE + 1);

        return new AdminUserPageResponseDto(
                users.getContent().stream()
                        .map(user -> AdminUserListItemResponseDto.from(
                                user,
                                rolesByUserId.getOrDefault(user.getId(), List.of())
                        ))
                        .toList(),
                currentPage,
                users.getSize(),
                users.getTotalElements(),
                totalPages,
                users.isFirst(),
                users.isLast(),
                startPage,
                endPage
        );
    }
}
