package com.commerce.pagopa.identity.infrastructure.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.identity.infrastructure.jwt.JwtAuthenticationFilter;
import com.commerce.pagopa.identity.infrastructure.oauth.handler.SellerOAuth2LoginSuccessHandler;
import com.commerce.pagopa.identity.infrastructure.oauth.service.CustomOAuth2UserService;

@Configuration
@RequiredArgsConstructor
public class SellerSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final SellerOAuth2LoginSuccessHandler sellerOAuth2LoginSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    @Order(2)
    public SecurityFilterChain sellerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(
                        exception -> exception
                                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/seller/login"))
                                .accessDeniedPage("/seller/login?denied")
                )
                .securityMatcher(
                        "/seller",
                        "/seller/**",
                        "/seller-assets/**",
                        "/oauth2/authorization/google-seller",
                        "/login/oauth2/code/google-seller"
                )
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(
                                        "/seller/login",
                                        "/seller-assets/**",
                                        "/oauth2/authorization/google-seller",
                                        "/login/oauth2/code/google-seller"
                                )
                                .permitAll()
                                .requestMatchers(
                                        "/seller",
                                        "/seller/**"
                                )
                                .hasRole("SELLER")
                                .anyRequest()
                                .hasRole("SELLER")
                )
                .oauth2Login(
                        oauth2Login -> oauth2Login.loginPage("/seller/login")
                                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                                .successHandler(sellerOAuth2LoginSuccessHandler)
                                .failureUrl("/seller/login?error")
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
