package com.commerce.pagopa.identity.infrastructure.jwt;

import jakarta.servlet.FilterChain;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commerce.pagopa.identity.application.JwtAuthenticationService;
import com.commerce.pagopa.identity.infrastructure.jwt.resolver.TokenResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenResolver tokenResolver;
    @Mock
    private JwtAuthenticationService jwtAuthenticationService;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @ParameterizedTest
    @ValueSource(strings = {
            "/admin-assets/css/admin.css",
            "/admin-assets/vendor/tabler/1.4.0/tabler.min.js",
            "/seller-assets/css/seller.css",
            "/seller-assets/js/seller.js",
            "/images/default.png"
    })
    void staticResource_should_skip_jwt_authentication(String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(jwtAuthenticationFilter.shouldNotFilter(request)).isTrue();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(
                tokenResolver,
                jwtTokenProvider,
                jwtAuthenticationService
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/seller/products",
            "/api/v1/products",
            "/api/v1/images/1"
    })
    void applicationRequest_should_be_filtered(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);

        assertThat(jwtAuthenticationFilter.shouldNotFilter(request)).isFalse();
    }
}
