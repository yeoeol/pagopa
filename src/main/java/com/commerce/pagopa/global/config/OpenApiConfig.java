package com.commerce.pagopa.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String jwtSchemeName = "JWT Bearer Auth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(getInfo())
                .addSecurityItem(getSecurityRequirement())
                .components(getComponents());
    }

    private Info getInfo() {
        return new Info()
                .title("Pagopa API Server")
                .description("Pagopa REST API Specification")
                .version("v1");
    }

    private Components getComponents() {
        SecurityScheme securityScheme = getSecurityScheme();
        return new Components().addSecuritySchemes(jwtSchemeName, securityScheme);
    }

    private SecurityScheme getSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Authorization 헤더에 토큰 입력 (Bearer를 제외한 토큰만 입력)");
    }

    private SecurityRequirement getSecurityRequirement() {
        return new SecurityRequirement().addList(jwtSchemeName);
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("0. 전체보기")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public GroupedOpenApi roleApi() {
        return GroupedOpenApi.builder()
                .group("<관리자> 관리자용 API 관리")
                .pathsToMatch("/api/v1/roles/**")
                .build();
    }

    @Bean
    public GroupedOpenApi sellerApi() {
        return GroupedOpenApi.builder()
                .group("<판매자> 판매자용 API 관리")
                .pathsToMatch("/api/v1/sellers/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("인증 관리")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("회원 정보 관리")
                .pathsToMatch("/api/v1/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi categoryApi() {
        return GroupedOpenApi.builder()
                .group("카테고리 관리")
                .pathsToMatch("/api/v1/categories/**")
                .build();
    }

    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
                .group("상품 관리")
                .pathsToMatch("/api/v1/products/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cartApi() {
        return GroupedOpenApi.builder()
                .group("장바구니 관리")
                .pathsToMatch("/api/v1/cart/**", "/api/v1/cart-items/**")
                .build();
    }

    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("주문 관리")
                .pathsToMatch("/api/v1/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi paymentApi() {
        return GroupedOpenApi.builder()
                .group("결제 관리")
                .pathsToMatch("/api/v1/payments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reviewApi() {
        return GroupedOpenApi.builder()
                .group("리뷰 관리")
                .pathsToMatch("/api/v1/reviews/**")
                .build();
    }

    @Bean
    public GroupedOpenApi searchHistoryApi() {
        return GroupedOpenApi.builder()
                .group("검색 기록 관리")
                .pathsToMatch("/api/v1/search-histories/**")
                .build();
    }

    @Bean
    public GroupedOpenApi imageApi() {
        return GroupedOpenApi.builder()
                .group("이미지 관리")
                .pathsToMatch("/api/v1/images/**")
                .build();
    }
}
