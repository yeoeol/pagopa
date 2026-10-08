package com.commerce.pagopa.identity.infrastructure.jwt.resolver;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Primary
@Component
@RequiredArgsConstructor
public class ChainTokenResolver implements TokenResolver {

    private final List<TokenResolver> resolvers;

    @Override
    public String resolveToken(HttpServletRequest request) {
        for (TokenResolver resolver : resolvers) {
            if (resolver instanceof ChainTokenResolver)
                continue; // 자기 자신 제외
            String token = resolver.resolveToken(request);
            if (token != null) {
                return token;
            }
        }
        return null;
    }
}
