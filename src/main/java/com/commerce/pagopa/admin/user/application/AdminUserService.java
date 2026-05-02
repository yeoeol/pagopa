package com.commerce.pagopa.admin.user.application;

import com.commerce.pagopa.admin.user.application.dto.response.UserResponseDto;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.RefreshTokenRepository;
import com.commerce.pagopa.user.domain.repository.UserRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.ban.seconds}")
    private long banSeconds;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> findAll(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(UserResponseDto::from);
    }

    @Transactional
    public void ban(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);
        user.ban(banSeconds);
    }

    @Transactional
    public void unban(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);
        user.unban();
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);

        refreshTokenRepository.deleteByUserId(userId);

        user.withdraw();
    }

    @Transactional
    public void updateSellerRole(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);
        user.updateSellerRole();
    }
}
