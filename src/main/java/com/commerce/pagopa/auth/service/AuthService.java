package com.commerce.pagopa.auth.service;

import com.commerce.pagopa.auth.jwt.JwtTokenProvider;
import com.commerce.pagopa.auth.jwt.TokenResponseDto;
import com.commerce.pagopa.user.domain.model.RefreshToken;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.RefreshTokenRepository;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
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
        ErrorCode tokenValidationErrorCode = jwtTokenProvider.getTokenValidationErrorCode(refreshToken);
        if (tokenValidationErrorCode == null) {
            RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

            // 기존에 저장되어 있던 토큰과 파라미터로 넘어온 토큰이 일치하는지 검증
            if (!token.getToken().equals(refreshToken)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }

            Long userId = token.getUserId();
            User user = userRepository.findByIdOrThrow(userId);

            return issueAccessTokenAndRefreshToken(userId, user.getEmail(), user.getRoleName());
        }

        throw new BusinessException(tokenValidationErrorCode);
    }

    @Transactional
    public TokenResponseDto issueAccessTokenAndRefreshToken(Long userId, String email, String role) {
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

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);

        refreshTokenRepository.deleteByUserId(userId);

        user.withdraw();
    }
}
