package com.commerce.pagopa.auth.jwt;

import com.commerce.pagopa.auth.handler.ApiAuthenticationEntryPoint;
import com.commerce.pagopa.auth.jwt.resolver.TokenResolver;
import com.commerce.pagopa.auth.service.JwtAuthenticationService;
import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenResolver tokenResolver;
    private final JwtAuthenticationService jwtAuthenticationService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = tokenResolver.resolveToken(request);

        if (token != null) {
            ErrorCode tokenValidationErrorCode = jwtTokenProvider.getTokenValidationErrorCode(token);

            if (tokenValidationErrorCode == null) {
                Long userId = jwtTokenProvider.getUserId(token);
                try {
                    AuthenticatedUser user = jwtAuthenticationService.loadActiveUser(userId);

                    CustomUserDetails principal = new CustomUserDetails(
                            user.userId(),
                            user.email(),
                            user.roleCodes()
                    );

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal,
                            "",
                            principal.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (BusinessException e) {
                    tokenValidationErrorCode = ErrorCode.USER_NOT_ACTIVE;

                    SecurityContextHolder.clearContext();
                    request.setAttribute(
                            ApiAuthenticationEntryPoint.AUTH_ERROR_CODE_ATTRIBUTE,
                            tokenValidationErrorCode
                    );
                }
            } else {
                SecurityContextHolder.clearContext();
                request.setAttribute(
                        ApiAuthenticationEntryPoint.AUTH_ERROR_CODE_ATTRIBUTE,
                        tokenValidationErrorCode
                );
            }
        }

        filterChain.doFilter(request, response);
    }
}
