package com.commerce.pagopa.auth.oauth.handler;

import com.commerce.pagopa.auth.jwt.JwtTokenProvider;
import com.commerce.pagopa.domain.user.entity.RefreshToken;
import com.commerce.pagopa.domain.user.repository.RefreshTokenRepository;
import com.commerce.pagopa.auth.oauth.CustomOAuth2User;
import com.commerce.pagopa.global.util.JwtCookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.generateAccessToken(
                oAuth2User.getUserId(), oAuth2User.getEmail(), oAuth2User.getRole()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(oAuth2User.getUserId());

        // RefreshToken이 존재하면 업데이트, 없으면 새로 생성 후 저장
        refreshTokenRepository.findByUserId(oAuth2User.getUserId())
                .ifPresentOrElse(
                        rt -> rt.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(RefreshToken.create(
                                oAuth2User.getUserId(),
                                refreshToken
                        ))
                );

        response.addCookie(JwtCookieUtil.createJwtCookie(accessToken, jwtTokenProvider.getAccessTokenExpiry()));
        response.sendRedirect("http://localhost:3000/oauth2/redirect", HttpServletResponse.SC_FOUND);
    }
}
