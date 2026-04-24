package com.commerce.pagopa.auth.oauth.handler;

import com.commerce.pagopa.auth.jwt.JwtTokenProvider;
import com.commerce.pagopa.auth.jwt.JwtTokenType;
import com.commerce.pagopa.auth.jwt.TokenResponseDto;
import com.commerce.pagopa.auth.service.AuthService;
import com.commerce.pagopa.auth.oauth.CustomOAuth2User;
import com.commerce.pagopa.global.util.JwtCookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.redirect-url}")
    private String oauth2RedirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        TokenResponseDto tokenResponseDto = authService.issueAccessTokenAndRefreshToken(
                oAuth2User.getUserId(),
                oAuth2User.getEmail(),
                oAuth2User.getRole()
        );

        Cookie accessTokenCookie = JwtCookieUtil.createJwtCookie(
                JwtTokenType.ACCESS_TOKEN,
                tokenResponseDto.accessToken(),
                jwtTokenProvider.getAccessTokenExpiry() / 1000
        );
        Cookie refreshTokenCookie = JwtCookieUtil.createJwtCookie(
                JwtTokenType.REFRESH_TOKEN,
                tokenResponseDto.refreshToken(),
                jwtTokenProvider.getRefreshTokenExpiry() / 1000
        );
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        response.sendRedirect(oauth2RedirectUrl);
    }
}
