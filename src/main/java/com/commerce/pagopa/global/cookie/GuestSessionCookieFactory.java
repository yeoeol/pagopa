package com.commerce.pagopa.global.cookie;

import com.commerce.pagopa.global.config.CookieSettings;
import com.commerce.pagopa.global.util.CookieUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.UUID;

// TODO: setSecure() -> HTTPS, setSameSite() -> CSRF 적용
@Component
@RequiredArgsConstructor
public class GuestSessionCookieFactory {

    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 30;

    private final CookieSettings cookieSettings;

    public String getOrCreateGuestSessionId(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = CookieUtil.getSessionIdFromCookie(request);

        if (sessionId != null) {
            return sessionId;
        }

        String newSessionId = UUID.randomUUID().toString();
        response.addCookie(createGuestSessionCookie(newSessionId));
        return newSessionId;
    }

    private Cookie createGuestSessionCookie(String sessionId) {
        Cookie cookie = new Cookie(CookieUtil.GUEST_SESSION_COOKIE, sessionId);
        cookie.setPath("/");
        cookie.setHttpOnly(cookieSettings.isHttpOnly());
        cookie.setMaxAge(COOKIE_MAX_AGE);
        return cookie;
    }
}
