package com.commerce.pagopa.domain.user.service;

import com.commerce.pagopa.domain.image.service.ImageService;
import com.commerce.pagopa.domain.user.dto.request.UserUpdateRequestDto;
import com.commerce.pagopa.domain.user.dto.response.UserResponseDto;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import com.commerce.pagopa.global.exception.UserNotFountException;
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
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFountException::new);
        return UserResponseDto.from(user);
    }

    @Transactional
    public UserResponseDto update(Long userId, UserUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFountException::new);

        // 기존 이미지 삭제
        if (StringUtils.hasText(requestDto.profileImage())) {
            imageService.delete(user.getProfileImage());
        }

        user.updateProfile(requestDto.nickname(), requestDto.profileImage());
        return UserResponseDto.from(user);
    }
}
