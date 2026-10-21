package com.commerce.pagopa.identity.infrastructure.oauth.handler;

import java.io.IOException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.identity.application.AuthService;
import com.commerce.pagopa.identity.domain.RoleCode;
import com.commerce.pagopa.identity.infrastructure.jwt.JwtCookieFactory;
import com.commerce.pagopa.identity.infrastructure.jwt.JwtTokenProvider;
import com.commerce.pagopa.identity.infrastructure.jwt.JwtTokenType;
import com.commerce.pagopa.identity.infrastructure.jwt.TokenResponseDto;
import com.commerce.pagopa.identity.infrastructure.oauth.CustomOAuth2User;

@Component
@RequiredArgsConstructor
public class SellerOAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtCookieFactory jwtCookieFactory;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        if (!oAuth2User.getRoles()
                .contains(RoleCode.ROLE_SELLER.name())) {
            clearSession(request);
            response.sendRedirect(request.getContextPath() + "/seller/login?denied");
            return;
        }

        TokenResponseDto tokenResponseDto = authService.issueAccessTokenAndRefreshToken(
                oAuth2User.getUserId(),
                oAuth2User.getEmail(),
                oAuth2User.getRoles()
        );

        Cookie accessTokenCookie = jwtCookieFactory.createJwtCookie(
                JwtTokenType.ACCESS_TOKEN,
                tokenResponseDto.accessToken(),
                jwtTokenProvider.getAccessTokenExpiry() / 1000
        );
        Cookie refreshTokenCookie = jwtCookieFactory.createJwtCookie(
                JwtTokenType.REFRESH_TOKEN,
                tokenResponseDto.refreshToken(),
                jwtTokenProvider.getRefreshTokenExpiry() / 1000
        );
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        clearSession(request);
        response.sendRedirect(request.getContextPath() + "/seller/products");
    }

    private void clearSession(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
