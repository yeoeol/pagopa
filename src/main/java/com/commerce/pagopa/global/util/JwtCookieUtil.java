package com.commerce.pagopa.global.util;

import com.commerce.pagopa.auth.jwt.JwtTokenType;
import jakarta.servlet.http.Cookie;

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
}
