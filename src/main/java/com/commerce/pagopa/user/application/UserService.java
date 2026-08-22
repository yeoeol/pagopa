package com.commerce.pagopa.user.application;

import com.commerce.pagopa.image.application.ImageService;
import com.commerce.pagopa.role.application.RoleService;
import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.user.application.dto.request.UserCreateRequestDto;
import com.commerce.pagopa.user.application.dto.request.UserUpdateRequestDto;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import com.commerce.pagopa.userrole.domain.model.UserRole;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageService imageService;
    private final RoleService roleService;

    @Transactional
    public User register(UserCreateRequestDto requestDto) {
        User user = User.create(
                requestDto.provider(),
                requestDto.providerId(),
                requestDto.name(),
                requestDto.email(),
                requestDto.profileImageUrl()
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
            imageService.delete(user.getProfileImageUrl());
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
