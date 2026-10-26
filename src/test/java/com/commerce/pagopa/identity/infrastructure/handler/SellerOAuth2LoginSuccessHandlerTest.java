package com.commerce.pagopa.identity.infrastructure.handler;

import java.util.Set;

import jakarta.servlet.http.Cookie;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.commerce.pagopa.global.config.CookieSettings;
import com.commerce.pagopa.identity.application.AuthService;
import com.commerce.pagopa.identity.domain.RoleCode;
import com.commerce.pagopa.identity.infrastructure.jwt.JwtCookieFactory;
import com.commerce.pagopa.identity.infrastructure.jwt.JwtTokenProvider;
import com.commerce.pagopa.identity.infrastructure.jwt.TokenResponseDto;
import com.commerce.pagopa.identity.infrastructure.oauth.CustomOAuth2User;
import com.commerce.pagopa.identity.infrastructure.oauth.handler.SellerOAuth2LoginSuccessHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class SellerOAuth2LoginSuccessHandlerTest {

    private AuthService authService;
    private JwtTokenProvider jwtTokenProvider;
    private SellerOAuth2LoginSuccessHandler handler;

    @BeforeEach
    void setUpBefore() {
        authService = mock(AuthService.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        handler = new SellerOAuth2LoginSuccessHandler(
                authService,
                jwtTokenProvider,
                new JwtCookieFactory(new CookieSettings(true))
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sellerLogin_withSellerRole_setsTokensAndRedirectsToProducts() throws Exception {
        Authentication authentication = mock(Authentication.class);
        CustomOAuth2User oAuth2User = mock(CustomOAuth2User.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession();

        given(authentication.getPrincipal()).willReturn(oAuth2User);
        given(oAuth2User.getUserId()).willReturn(1L);
        given(oAuth2User.getEmail()).willReturn("seller@example.com");
        given(oAuth2User.getRoles()).willReturn(Set.of(RoleCode.ROLE_SELLER.name()));
        given(
                authService.issueAccessTokenAndRefreshToken(
                        1L,
                        "seller@example.com",
                        Set.of(RoleCode.ROLE_SELLER.name())
                )
        ).willReturn(TokenResponseDto.of(
                1L,
                "access-token",
                "refresh-token"
        ));
        given(jwtTokenProvider.getAccessTokenExpiry()).willReturn(3_600_000L);
        given(jwtTokenProvider.getRefreshTokenExpiry()).willReturn(86_400_000L);

        handler.onAuthenticationSuccess(
                request,
                response,
                authentication
        );

        Cookie accessTokenCookie = response.getCookie("accessToken");
        Cookie refreshTokenCookie = response.getCookie("refreshToken");
        assertThat(response.getRedirectedUrl()).isEqualTo("/seller/products");
        assertThat(accessTokenCookie).isNotNull();
        assertThat(accessTokenCookie.getValue()).isEqualTo("access-token");
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(refreshTokenCookie.getValue()).isEqualTo("refresh-token");
        assertThat(request.getSession(false)).isNull();
    }

    @Test
    void sellerLogin_withoutSellerRole_redirectsToDeniedPage() throws Exception {
        Authentication authentication = mock(Authentication.class);
        CustomOAuth2User oAuth2User = mock(CustomOAuth2User.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(authentication.getPrincipal()).willReturn(oAuth2User);
        given(oAuth2User.getRoles()).willReturn(Set.of(RoleCode.ROLE_USER.name()));

        handler.onAuthenticationSuccess(
                request,
                response,
                authentication
        );

        assertThat(response.getRedirectedUrl()).isEqualTo("/seller/login?denied");
        assertThat(response.getCookies()).isEmpty();
        verifyNoInteractions(
                authService,
                jwtTokenProvider
        );
    }
}
