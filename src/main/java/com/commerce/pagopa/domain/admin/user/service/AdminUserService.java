package com.commerce.pagopa.domain.admin.user.service;

import com.commerce.pagopa.domain.admin.user.dto.response.UserResponseDto;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import com.commerce.pagopa.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Value("${app.ban.seconds}")
    private long banSeconds;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> findAll(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(UserResponseDto::from);
    }

    @Transactional
    public void ban(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        user.ban(banSeconds);
    }

    @Transactional
    public void unban(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        user.unban();
    }

    @Transactional
    public void withdrawn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        user.withdrawn();
    }

    @Transactional
    public void updateSellerRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        user.updateSellerRole();
    }
}
