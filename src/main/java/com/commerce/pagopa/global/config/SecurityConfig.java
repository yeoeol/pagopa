package com.commerce.pagopa.global.config;

import com.commerce.pagopa.auth.jwt.JwtAuthenticationFilter;
import com.commerce.pagopa.auth.oauth.handler.OAuth2LoginFailureHandler;
import com.commerce.pagopa.auth.oauth.handler.OAuth2LoginSuccessHandler;
import com.commerce.pagopa.auth.oauth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 1. 카테고리 (Category) 패키지: 조회는 모두 허용, 그 외(생성/수정/삭제)는 ADMIN만
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers("/api/v1/categories/**").hasRole("ADMIN")

                        // 2. 상품 (Product) 패키지: 조회는 모두 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        // 상품 등록(POST), 수정/삭제는 SELLER, ADMIN만 (@PreAuthorize와 함께 사용)
                        .requestMatchers("/api/v1/products/**").hasAnyRole("SELLER", "ADMIN")

                        // 3. User, Cart, Image 패키지: 인증된 사용자(모든 권한) 인가
                        .requestMatchers("/api/v1/users/**", "/api/v1/cart/**", "/api/v1/images/**").authenticated()

                        // 그 외의 모든 요청은 로그인 필요
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2Login -> oauth2Login
                        .userInfoEndpoint(endpoint ->
                                endpoint.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                )
                .oauth2Client(Customizer.withDefaults())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
