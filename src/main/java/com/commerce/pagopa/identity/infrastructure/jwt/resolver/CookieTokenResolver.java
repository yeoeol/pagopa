package com.commerce.pagopa.identity.infrastructure.jwt.resolver;

import com.commerce.pagopa.identity.infrastructure.jwt.JwtTokenType;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
@Order(1)
public class CookieTokenResolver implements TokenResolver {

    @Override
    public String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (JwtTokenType.ACCESS_TOKEN.getCookieName().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
