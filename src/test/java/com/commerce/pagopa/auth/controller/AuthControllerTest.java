package com.commerce.pagopa.auth.controller;

import com.commerce.pagopa.auth.jwt.JwtTokenProvider;
import com.commerce.pagopa.auth.jwt.TokenResponseDto;
import com.commerce.pagopa.auth.service.AuthService;
import com.commerce.pagopa.global.config.CookieSettings;
import com.commerce.pagopa.global.cookie.JwtCookieFactory;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.global.util.JwtCookieUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthService authService;
    private JwtTokenProvider jwtTokenProvider;
    private AuthController authController;

    @BeforeEach
    void setUpBefore() {
        authService = mock(AuthService.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        authController = new AuthController(
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
    void logout_clearsAccessTokenCookieAndDeletesRefreshToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<ApiResponse<Void>> result = authController.logout(1L, request, response);

        Cookie cookie = response.getCookie(JwtCookieUtil.ACCESS_TOKEN_COOKIE_NAME);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge()).isZero();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(request.getSession(false)).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(authService).logout(1L);
    }

    @Test
    void refresh_withValidRefreshTokenCookie_returnsOkAndSetsBothCookies() {
        // given
        String refreshTokenValue = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        long accessTokenExpiryMs = 3_600_000L;   // 3600초 (ms 단위)
        long refreshTokenExpiryMs = 86_400_000L; // 86400초 (ms 단위)

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", refreshTokenValue));
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(authService.reissueToken(refreshTokenValue))
                .willReturn(TokenResponseDto.of(1L, newAccessToken, newRefreshToken));
        given(jwtTokenProvider.getAccessTokenExpiry()).willReturn(accessTokenExpiryMs);
        given(jwtTokenProvider.getRefreshTokenExpiry()).willReturn(refreshTokenExpiryMs);

        // when
        ResponseEntity<ApiResponse<Void>> result = authController.refresh(request, response);

        // then
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();

        Cookie accessCookie = response.getCookie("accessToken");
        assertThat(accessCookie).isNotNull();
        assertThat(accessCookie.getValue()).isEqualTo(newAccessToken);
        assertThat(accessCookie.getMaxAge()).isEqualTo((int) (accessTokenExpiryMs / 1000));
        assertThat(accessCookie.isHttpOnly()).isTrue();
        assertThat(accessCookie.getPath()).isEqualTo("/");

        Cookie refreshCookie = response.getCookie("refreshToken");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isEqualTo(newRefreshToken);
        assertThat(refreshCookie.getMaxAge()).isEqualTo((int) (refreshTokenExpiryMs / 1000));
        assertThat(refreshCookie.isHttpOnly()).isTrue();
        assertThat(refreshCookie.getPath()).isEqualTo("/");

        verify(authService, times(1)).reissueToken(refreshTokenValue);
    }

    @Test
    void refresh_withNoCookies_throwsBusinessException() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest(); // 쿠키 없음 → getCookies() == null
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when & then
        assertThatThrownBy(() -> authController.refresh(request, response))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void refresh_withOnlyAccessTokenCookie_throwsBusinessException() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("accessToken", "some-access-token")); // refreshToken 쿠키 없음
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when & then
        assertThatThrownBy(() -> authController.refresh(request, response))
                .isInstanceOf(BusinessException.class);
    }
}
