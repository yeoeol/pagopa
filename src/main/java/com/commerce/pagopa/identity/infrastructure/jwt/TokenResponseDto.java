package com.commerce.pagopa.identity.infrastructure.jwt;

public record TokenResponseDto(
        Long userId,
        String accessToken,
        String refreshToken
) {
    public static TokenResponseDto of(Long userId, String accessToken, String refreshToken) {
        return new TokenResponseDto(
                userId,
                accessToken,
                refreshToken
        );
    }
}
