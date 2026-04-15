package com.commerce.pagopa.auth.jwt.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component
@RequiredArgsConstructor
public class ChainTokenResolver implements TokenResolver {

    private final List<TokenResolver> resolvers;

    @Override
    public String resolveToken(HttpServletRequest request) {
        for (TokenResolver resolver : resolvers) {
            if (resolver instanceof ChainTokenResolver) continue; // 자기 자신 제외
            String token = resolver.resolveToken(request);
            if (token != null) {
                return token;
            }
        }
        return null;
    }
}
