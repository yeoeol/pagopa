package com.commerce.pagopa.auth.handler;

import com.commerce.pagopa.global.response.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ApiAuthenticationEntryPointTest {

    private final JsonMapper jsonMapper = new JsonMapper();
    private final ApiAuthenticationEntryPoint authenticationEntryPoint =
            new ApiAuthenticationEntryPoint(jsonMapper);

    @Test
    void commence_returnsUnauthorizedResponse_whenNoAuthenticationErrorAttributeExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/me");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authenticationEntryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("Full authentication is required")
        );

        JsonNode body = jsonMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(ErrorCode.UNAUTHORIZED.getHttpStatus().value());
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("error").get("code").asString()).isEqualTo(ErrorCode.UNAUTHORIZED.getCode());
        assertThat(body.get("error").get("message").asString()).isEqualTo(ErrorCode.UNAUTHORIZED.getMessage());
    }

    @Test
    void commence_returnsInvalidTokenResponse_whenInvalidTokenAttributeExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/me");
        request.setAttribute(ApiAuthenticationEntryPoint.AUTH_ERROR_CODE_ATTRIBUTE, ErrorCode.INVALID_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        authenticationEntryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("Invalid token")
        );

        JsonNode body = jsonMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(ErrorCode.INVALID_TOKEN.getHttpStatus().value());
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("error").get("code").asString()).isEqualTo(ErrorCode.INVALID_TOKEN.getCode());
        assertThat(body.get("error").get("message").asString()).isEqualTo(ErrorCode.INVALID_TOKEN.getMessage());
    }

    @Test
    void commence_returnsExpiredTokenResponse_whenExpiredTokenAttributeExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/me");
        request.setAttribute(ApiAuthenticationEntryPoint.AUTH_ERROR_CODE_ATTRIBUTE, ErrorCode.EXPIRED_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        authenticationEntryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("Expired token")
        );

        JsonNode body = jsonMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(ErrorCode.EXPIRED_TOKEN.getHttpStatus().value());
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("error").get("code").asString()).isEqualTo(ErrorCode.EXPIRED_TOKEN.getCode());
        assertThat(body.get("error").get("message").asString()).isEqualTo(ErrorCode.EXPIRED_TOKEN.getMessage());
    }
}
