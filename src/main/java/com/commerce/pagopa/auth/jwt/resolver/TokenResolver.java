package com.commerce.pagopa.auth.jwt.resolver;

import jakarta.servlet.http.HttpServletRequest;

public interface TokenResolver {
    String resolveToken(HttpServletRequest request);
}
