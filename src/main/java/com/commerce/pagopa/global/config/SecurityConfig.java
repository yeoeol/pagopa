package com.commerce.pagopa.global.config;

import com.commerce.pagopa.auth.handler.ApiAuthenticationEntryPoint;
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
    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
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
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers("/api/v1/categories/**").hasRole("ADMIN")

                        .requestMatchers("/api/v1/images/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/api/v1/products/**").permitAll()

                        .requestMatchers("/api/v1/users/**").hasAnyRole("USER", "SELLER")

                        .requestMatchers("/api/v1/cart/**").hasRole("USER")

                        .requestMatchers("/api/v1/orders/**").hasRole("USER")

                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/products/**").hasAnyRole("USER", "SELLER")
                        .requestMatchers("/api/v1/reviews/**").hasRole("USER")

                        .requestMatchers("/api/v1/scraps/**").hasRole("USER")

                        .requestMatchers("/api/v1/search-histories/**").permitAll()

                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        .requestMatchers("/actuator/health/**", "/actuator/prometheus").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        .requestMatchers("/api/v1/seller/**").hasRole("SELLER")

                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/**").authenticated()

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
