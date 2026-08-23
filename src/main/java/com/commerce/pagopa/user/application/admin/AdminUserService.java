package com.commerce.pagopa.user.application.admin;

import com.commerce.pagopa.user.application.admin.dto.request.AdminUserSearchRequestDto;
import com.commerce.pagopa.user.application.admin.dto.response.AdminUserDetailResponseDto;
import com.commerce.pagopa.user.application.admin.dto.response.AdminUserPageResponseDto;
import com.commerce.pagopa.user.application.admin.dto.response.AdminUserRoleResponseDto;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import com.commerce.pagopa.userrole.domain.model.UserRole;
import com.commerce.pagopa.userrole.domain.repository.UserRoleRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional(readOnly = true)
    public AdminUserPageResponseDto search(AdminUserSearchRequestDto requestDto) {
        int page = requestDto.page() == null ? 0 : requestDto.page();
        int size = requestDto.size() == null ? DEFAULT_PAGE_SIZE : requestDto.size();
        String keyword = normalizeKeyword(requestDto.keyword());

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );
        Page<User> users = userRepository.searchAdminUsers(
                keyword,
                requestDto.status(),
                pageable
        );

        List<Long> userIds = users.getContent()
                .stream()
                .map(User::getId)
                .toList();

        Map<Long, List<AdminUserRoleResponseDto>> rolesByUserId =
                findRolesByUserIds(userIds);

        return AdminUserPageResponseDto.from(
                users,
                rolesByUserId
        );
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponseDto find(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);

        Map<Long, List<AdminUserRoleResponseDto>> rolesByUserId =
                findRolesByUserIds(List.of(userId));

        return AdminUserDetailResponseDto.from(
                user,
                rolesByUserId.getOrDefault(user.getId(), List.of())
        );
    }

    @Transactional
    public void activate(Long userId) {
        User user = userRepository.findByIdForUpdateOrThrow(userId);
        user.activate(Instant.now());
    }

    @Transactional
    public void suspend(Long userId) {
        User user = userRepository.findByIdForUpdateOrThrow(userId);
        user.suspend(Instant.now());
    }

    @Transactional
    public void ban(Long userId) {
        User user = userRepository.findByIdForUpdateOrThrow(userId);
        user.ban(Instant.now());
    }

    private Map<Long, List<AdminUserRoleResponseDto>> findRolesByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        List<UserRole> userRoles = userRoleRepository.findAllWithRoleByUserIds(userIds);
        return userRoles.stream()
                .collect(Collectors.groupingBy(
                        userRole -> userRole.getUser().getId(),
                        Collectors.mapping(
                                AdminUserRoleResponseDto::from,
                                Collectors.toList()
                        )
                ));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
