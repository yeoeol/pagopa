package com.commerce.pagopa.user.application;

import com.commerce.pagopa.image.application.ImageService;
import com.commerce.pagopa.user.application.dto.request.UserUpdateRequestDto;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageService imageService;

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
            imageService.delete(user.getProfileImage());
        }

        user.updateProfile(requestDto.nickname(), requestDto.profileImage());
        return UserResponseDto.from(user);
    }
}
