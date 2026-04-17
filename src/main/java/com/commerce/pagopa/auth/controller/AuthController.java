package com.commerce.pagopa.auth.controller;

import com.commerce.pagopa.auth.service.AuthService;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.global.util.JwtCookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(userId);
        response.addCookie(JwtCookieUtil.deleteJwtCookie());

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(ApiResponse.ok());
    }
}
