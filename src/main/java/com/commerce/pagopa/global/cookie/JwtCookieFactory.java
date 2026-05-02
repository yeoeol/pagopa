package com.commerce.pagopa.global.cookie;

import com.commerce.pagopa.auth.jwt.JwtTokenType;
import com.commerce.pagopa.global.config.CookieSettings;

import jakarta.servlet.http.Cookie;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

// TODO: setSecure() -> HTTPS, setSameSite() -> CSRF 적용
@Component
@RequiredArgsConstructor
public class JwtCookieFactory {

    private final CookieSettings cookieSettings;

    public Cookie createJwtCookie(JwtTokenType tokenType, String jwt, long tokenExpirySeconds) {
        Cookie cookie = new Cookie(tokenType.getCookieName(), jwt);
        cookie.setPath("/");
        cookie.setHttpOnly(cookieSettings.isHttpOnly());
        cookie.setMaxAge((int) tokenExpirySeconds);
        return cookie;
    }

    public Cookie deleteJwtCookie(JwtTokenType tokenType) {
        Cookie cookie = new Cookie(tokenType.getCookieName(), "");
        cookie.setPath("/");
        cookie.setHttpOnly(cookieSettings.isHttpOnly());
        cookie.setMaxAge(0);
        return cookie;
    }
}
