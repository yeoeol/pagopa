package com.commerce.pagopa.auth.service;

import com.commerce.pagopa.auth.jwt.JwtTokenProvider;
import com.commerce.pagopa.auth.jwt.TokenResponseDto;
import com.commerce.pagopa.domain.user.entity.RefreshToken;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.RefreshTokenRepository;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import com.commerce.pagopa.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public TokenResponseDto reissueToken(String refreshToken) {
        boolean isValid = jwtTokenProvider.validateToken(refreshToken);
        if (isValid) {
            RefreshToken token = refreshTokenRepository.findByToken(refreshToken).orElseThrow();

            Long userId = token.getUserId();
            User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

            return issueAccessTokenAndRefreshToken(userId, user.getEmail(), user.getRoleName());
        }
        return null;
    }

    private TokenResponseDto issueAccessTokenAndRefreshToken(Long userId, String email, String role) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                userId, email, role
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        rt -> rt.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(RefreshToken.create(
                                userId,
                                refreshToken
                        ))
                );

        return TokenResponseDto.of(userId, accessToken, refreshToken);
    }
}
