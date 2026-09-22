package com.commerce.pagopa.identity.infrastructure.jwt.resolver;

import jakarta.servlet.http.HttpServletRequest;

public interface TokenResolver {
    String resolveToken(HttpServletRequest request);
}
