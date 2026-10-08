package com.commerce.pagopa.identity.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.cookie.GuestSessionCookieFactory;
import com.commerce.pagopa.identity.api.CurrentUser;
import com.commerce.pagopa.identity.api.UserIdentity;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final GuestSessionCookieFactory guestSessionCookieFactory;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType() == UserIdentity.class;
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy()
                .getContext()
                .getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails user) {
            return new UserIdentity(user.getUserId(), null);
        }

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

        String sessionId = guestSessionCookieFactory.getOrCreateGuestSessionId(request, response);

        return new UserIdentity(null, sessionId);
    }
}
