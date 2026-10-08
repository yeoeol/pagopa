package com.commerce.pagopa.identity.infrastructure.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtTokenType {
    ACCESS_TOKEN("accessToken"), REFRESH_TOKEN("refreshToken"),;

    private final String cookieName;
}
