package com.commerce.pagopa.global.config;

import com.commerce.pagopa.auth.oauth.handler.AdminOAuth2LoginSuccessHandler;
import com.commerce.pagopa.auth.oauth.service.CustomOAuth2UserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AdminSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final AdminOAuth2LoginSuccessHandler adminOAuth2LoginSuccessHandler;

    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/admin/access-denied")
                )

                .securityMatcher(
                        "/admin",
                        "/admin/**",
                        "/admin-assets/**",
                        "/oauth2/authorization/google-admin",
                        "/login/oauth2/code/google-admin"
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/admin/login",
                                "/admin/access-denied",
                                "/admin-assets/**",
                                "/oauth2/authorization/google-admin",
                                "/login/oauth2/code/google-admin"
                        ).permitAll()

                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/admin/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(adminOAuth2LoginSuccessHandler)
                        .failureUrl("/admin/login?error")
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }
}
