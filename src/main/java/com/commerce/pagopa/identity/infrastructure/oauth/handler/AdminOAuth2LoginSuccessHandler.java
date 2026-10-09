package com.commerce.pagopa.identity.infrastructure.oauth.handler;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.commerce.pagopa.identity.domain.RoleCode;

@Component
public class AdminOAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        boolean admin = authentication.getAuthorities()
                .stream()
                .anyMatch(
                        authority -> RoleCode.ROLE_ADMIN.name()
                                .equals(authority.getAuthority())
                );

        if (!admin) {
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/admin/access-denied");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
}
