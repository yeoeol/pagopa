package com.commerce.pagopa.auth.controller;

import com.commerce.pagopa.auth.jwt.JwtTokenProvider;
import com.commerce.pagopa.auth.jwt.JwtTokenType;
import com.commerce.pagopa.auth.jwt.TokenResponseDto;
import com.commerce.pagopa.auth.service.AuthService;
import com.commerce.pagopa.global.cookie.JwtCookieFactory;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.global.util.JwtCookieUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtCookieFactory jwtCookieFactory;

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(userId);
        clearCookieAndSession(request, response);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = JwtCookieUtil.extractTokenFromCookies(JwtTokenType.REFRESH_TOKEN, request.getCookies());
        TokenResponseDto tokenResponseDto = authService.reissueToken(refreshToken);

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

        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PatchMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.withdraw(userId);
        clearCookieAndSession(request, response);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Void>> checkAuthenticated() {
        return ResponseEntity.ok(ApiResponse.ok());
    }

    private void clearCookieAndSession(HttpServletRequest request, HttpServletResponse response) {
        response.addCookie(jwtCookieFactory.deleteJwtCookie(JwtTokenType.ACCESS_TOKEN));
        response.addCookie(jwtCookieFactory.deleteJwtCookie(JwtTokenType.REFRESH_TOKEN));

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}
