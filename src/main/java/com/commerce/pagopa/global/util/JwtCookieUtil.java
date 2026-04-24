package com.commerce.pagopa.global.util;

import com.commerce.pagopa.auth.jwt.JwtTokenType;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import jakarta.servlet.http.Cookie;

// TODO: setSecure() -> HTTPS, setSameSite() -> CSRF 적용
public class JwtCookieUtil {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    public static Cookie createJwtCookie(JwtTokenType tokenType, String jwt, long tokenExpirySeconds) {
        Cookie cookie = new Cookie(tokenType.getCookieName(), jwt);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) tokenExpirySeconds);
        return cookie;
    }

    public static Cookie deleteJwtCookie(JwtTokenType tokenType) {
        Cookie cookie = new Cookie(tokenType.getCookieName(), "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        return cookie;
    }

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
