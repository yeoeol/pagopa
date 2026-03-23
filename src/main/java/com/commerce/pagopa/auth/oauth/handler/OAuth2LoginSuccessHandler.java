package com.commerce.pagopa.auth.oauth.handler;

import com.commerce.pagopa.auth.jwt.JwtTokenProvider;
import com.commerce.pagopa.domain.user.entity.RefreshToken;
import com.commerce.pagopa.domain.user.repository.RefreshTokenRepository;
import com.commerce.pagopa.auth.oauth.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;

/*    @Value("${app.oauth2.authorized-redirect-uris[0]}")
    private String redirectUri;*/

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

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("accessToken", accessToken);
        paramMap.put("refreshToken", refreshToken);

        String json = objectMapper.writeValueAsString(paramMap);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
/*        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);*/
    }
}
