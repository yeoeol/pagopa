package com.commerce.pagopa.global.util;

import com.commerce.pagopa.auth.jwt.JwtTokenType;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

import jakarta.servlet.http.Cookie;

public class JwtCookieUtil {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    public static String extractTokenFromCookies(JwtTokenType tokenType, Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (tokenType.getCookieName().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }
}
