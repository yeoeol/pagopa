package com.commerce.pagopa.identity.application;

import com.commerce.pagopa.identity.application.dto.request.UserCreateRequestDto;
import com.commerce.pagopa.identity.application.dto.request.UserUpdateRequestDto;
import com.commerce.pagopa.identity.application.dto.response.UserResponseDto;
import com.commerce.pagopa.identity.domain.*;
import com.commerce.pagopa.media.api.ImageApi;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageApi imageApi;
    private final RoleService roleService;

    @Transactional
    public User register(UserCreateRequestDto requestDto) {
        User user = User.create(
				requestDto.provider(),
				requestDto.providerId(),
				requestDto.name(),
				requestDto.email(),
				requestDto.profileImageUrl(),
                LocalDateTime.now()
        );
        Role role = roleService.findUserRole();

        UserRole userRole = UserRole.create(user, role);
        user.addUserRole(userRole);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto find(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);
        return UserResponseDto.from(user);
    }

    @Transactional
    public UserResponseDto update(Long userId, UserUpdateRequestDto requestDto) {
        User user = userRepository.findByIdOrThrow(userId);

        // 기존 이미지 삭제
        if (StringUtils.hasText(requestDto.profileImage())) {
            imageApi.delete(user.getProfileImageUrl());
        }

        user.updateProfile(requestDto.name(), requestDto.profileImage());
        return UserResponseDto.from(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByProviderAndProviderId(
            Provider provider,
            String providerId
    ) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }
}
