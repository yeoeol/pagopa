package com.commerce.pagopa.auth.controller;

import com.commerce.pagopa.auth.jwt.JwtTokenProvider;
import com.commerce.pagopa.auth.service.AuthService;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.global.util.JwtCookieUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuthControllerTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void logout_clearsAccessTokenCookieAndDeletesRefreshToken() {
        AuthService authService = mock(AuthService.class);
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        AuthController authController = new AuthController(authService, jwtTokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
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
}
