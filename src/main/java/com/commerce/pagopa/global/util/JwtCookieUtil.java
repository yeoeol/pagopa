package com.commerce.pagopa.global.util;

import jakarta.servlet.http.Cookie;

public class JwtCookieUtil {

    public static Cookie createJwtCookie(String jwt, long accessTokenExpirySeconds) {
        Cookie cookie = new Cookie("accessToken", jwt);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) accessTokenExpirySeconds);
        return cookie;
    }
}
