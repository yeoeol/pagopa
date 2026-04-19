package com.commerce.pagopa.global.config;

import com.commerce.pagopa.auth.jwt.JwtAuthenticationFilter;
import com.commerce.pagopa.auth.oauth.handler.OAuth2LoginFailureHandler;
import com.commerce.pagopa.auth.oauth.handler.OAuth2LoginSuccessHandler;
import com.commerce.pagopa.auth.oauth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 카테고리 (Category): 조회는 전부 가능, 생성 등은 ADMIN만
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers("/api/v1/categories/**").hasRole("ADMIN")

                        // 이미지 (Image): 전부 가능
                        .requestMatchers("/api/v1/images/**").permitAll()

                        // 상품 (Product): 상품 등록(POST)은 SELLER, ADMIN만, 조회 등 나머지는 전부 가능
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/api/v1/products/**").permitAll()

                        // 유저 (User): SELLER, USER 가능
                        .requestMatchers("/api/v1/users/**").hasAnyRole("USER", "SELLER")

                        // 장바구니 (Cart): USER
                        .requestMatchers("/api/v1/cart/**").hasRole("USER")

                        // 주문 (Order): USER
                        .requestMatchers("/api/v1/orders/**").hasRole("USER")

                        // 리뷰 (Review)
                        // 상품별 리뷰 목록 API(GET /api/v1/reviews/products/{id})는 SELLER, USER
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/products/**").hasAnyRole("USER", "SELLER")
                        // 그 외 리뷰 관련: USER만 가능
                        .requestMatchers("/api/v1/reviews/**").hasRole("USER")

                        // 스크랩 (Scrap): USER
                        .requestMatchers("/api/v1/scraps/**").hasRole("USER")

                        // 검색 기록 (Search History): 모두 허용 (비로그인 대응)
                        .requestMatchers("/api/v1/search-histories/**").permitAll()

                        // 관리자 (ADMIN)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 판매자 (SELLER)
                        .requestMatchers("/api/v1/seller/**").hasRole("SELLER")

                        .requestMatchers("/api/v1/auth/**").authenticated()

                        // 그 외의 모든 요청은 인증 필요
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration conf = new CorsConfiguration();
        conf.setAllowedOrigins(allowedOrigins);
        conf.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        conf.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        conf.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", conf);

        return source;
    }
}