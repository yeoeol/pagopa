package com.commerce.pagopa.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public class CookieUtil {

    public static final String GUEST_SESSION_COOKIE = "GUEST_SESSION_ID";
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 30; // 30일 유지

    /**
     * 쿠키에서 비로그인 사용자용 세션 ID를 추출합니다.
     */
    public static String getSessionIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (GUEST_SESSION_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 비로그인 사용자용 세션 ID를 쿠키에서 찾고, 없다면 새로 생성하여 응답에 추가합니다.
     */
    public static String getOrCreateGuestSessionId(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = getSessionIdFromCookie(request);
        
        if (sessionId != null) {
            return sessionId;
        }

        // 쿠키가 없다면 새로 생성하여 응답에 추가
        String newSessionId = UUID.randomUUID().toString();
        Cookie cookie = new Cookie(GUEST_SESSION_COOKIE, newSessionId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);

        return newSessionId;
    }
}
