package com.commerce.pagopa.identity.infrastructure.handler;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import tools.jackson.databind.json.JsonMapper;

import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.global.response.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String AUTH_ERROR_CODE_ATTRIBUTE = "authErrorCode";

    private final JsonMapper jsonMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        ErrorCode errorCode = resolveErrorCode(request);

        log.warn("[AuthenticationEntryPoint] path={}, errorCode={}, message={}", request.getRequestURI(),
                errorCode.getCode(), authException.getMessage());

        response.setStatus(errorCode.getHttpStatus()
                .value());
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter()
                .write(jsonMapper.writeValueAsString(ApiResponse.error(errorCode)));
    }

    private ErrorCode resolveErrorCode(HttpServletRequest request) {
        Object errorCode = request.getAttribute(AUTH_ERROR_CODE_ATTRIBUTE);
        if (errorCode instanceof ErrorCode resolvedErrorCode) {
            return resolvedErrorCode;
        }
        return ErrorCode.UNAUTHORIZED;
    }
}
