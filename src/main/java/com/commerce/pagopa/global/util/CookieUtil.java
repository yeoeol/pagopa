package com.commerce.pagopa.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {

    public static final String GUEST_SESSION_COOKIE = "GUEST_SESSION_ID";

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
}
