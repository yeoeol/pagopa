package com.commerce.pagopa.user.application.admin;

import com.commerce.pagopa.user.application.admin.dto.request.AdminUserSearchRequestDto;
import com.commerce.pagopa.user.application.admin.dto.request.AdminUserSuspendRequestDto;
import com.commerce.pagopa.user.application.admin.dto.response.AdminUserDetailResponseDto;
import com.commerce.pagopa.user.application.admin.dto.response.AdminUserPageResponseDto;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AdminUserPageResponseDto search(AdminUserSearchRequestDto requestDto) {
        int page = requestDto.page() == null ? 0 : requestDto.page();
        int size = requestDto.size() == null ? DEFAULT_PAGE_SIZE : requestDto.size();
        String keyword = normalizeKeyword(requestDto.keyword());

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<User> users = userRepository.searchAdminUsers(
                keyword,
                requestDto.status(),
                pageable
        );

        return AdminUserPageResponseDto.from(users);
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponseDto find(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);
        return AdminUserDetailResponseDto.from(user);
    }

    @Transactional
    public void activate(Long userId) {
        User user = userRepository.findByIdForUpdateOrThrow(userId);
        user.activate();
    }

    @Transactional
    public void suspend(Long userId, AdminUserSuspendRequestDto requestDto) {
        User user = userRepository.findByIdForUpdateOrThrow(userId);
        user.suspend(requestDto.suspendedUntil());
    }

    @Transactional
    public void ban(Long userId) {
        User user = userRepository.findByIdForUpdateOrThrow(userId);
        user.ban();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
