package com.commerce.pagopa.global.util;

import jakarta.servlet.http.Cookie;

public class JwtCookieUtil {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    public static Cookie createJwtCookie(String jwt, long accessTokenExpirySeconds) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, jwt);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) accessTokenExpirySeconds);
        return cookie;
    }

    public static Cookie deleteJwtCookie() {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        return cookie;
    }
}
