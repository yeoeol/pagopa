package com.commerce.pagopa.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                .group("00. 전체보기")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1-01. 인증 관리")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("1-02. 사용자 관리")
                .pathsToMatch("/api/v1/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi categoryApi() {
        return GroupedOpenApi.builder()
                .group("1-03. 카테고리 관리")
                .pathsToMatch("/api/v1/categories/**")
                .build();
    }

    @Bean
    public GroupedOpenApi productsApi() {
        return GroupedOpenApi.builder()
                .group("1-04. 상품 관리")
                .pathsToMatch("/api/v1/products/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cartApi() {
        return GroupedOpenApi.builder()
                .group("1-05. 장바구니 관리")
                .pathsToMatch("/api/v1/cart/**")
                .build();
    }

    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("1-06. 주문 관리")
                .pathsToMatch("/api/v1/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi paymentApi() {
        return GroupedOpenApi.builder()
                .group("1-07. 결제 관리")
                .pathsToMatch("/api/v1/payments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reviewApi() {
        return GroupedOpenApi.builder()
                .group("1-08. 리뷰 관리")
                .pathsToMatch("/api/v1/reviews/**")
                .build();
    }

    @Bean
    public GroupedOpenApi scrapApi() {
        return GroupedOpenApi.builder()
                .group("1-09. 스크랩 관리")
                .pathsToMatch("/api/v1/scraps/**")
                .build();
    }

    @Bean
    public GroupedOpenApi searchHistoryApi() {
        return GroupedOpenApi.builder()
                .group("1-10. 검색 기록 관리")
                .pathsToMatch("/api/v1/search-histories/**")
                .build();
    }

    @Bean
    public GroupedOpenApi imageApi() {
        return GroupedOpenApi.builder()
                .group("1-11. 이미지 관리")
                .pathsToMatch("/api/v1/images/**")
                .build();
    }

    @Bean
    public GroupedOpenApi sellerProductApi() {
        return GroupedOpenApi.builder()
                .group("2-01. <판매자> 상품 관리")
                .pathsToMatch("/api/v1/seller/products/**")
                .build();
    }

    @Bean
    public GroupedOpenApi sellerOrderApi() {
        return GroupedOpenApi.builder()
                .group("2-02. <판매자> 주문 관리")
                .pathsToMatch("/api/v1/seller/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminUserApi() {
        return GroupedOpenApi.builder()
                .group("3-01. <관리자> 사용자 관리")
                .pathsToMatch("/api/v1/admin/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminCategoryApi() {
        return GroupedOpenApi.builder()
                .group("3-02. <관리자> 카테고리 관리")
                .pathsToMatch("/api/v1/admin/categories/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminProductApi() {
        return GroupedOpenApi.builder()
                .group("3-03. <관리자> 상품 관리")
                .pathsToMatch("/api/v1/admin/products/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminOrderApi() {
        return GroupedOpenApi.builder()
                .group("3-04. <관리자> 주문 관리")
                .pathsToMatch("/api/v1/admin/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminReviewApi() {
        return GroupedOpenApi.builder()
                .group("3-05. <관리자> 리뷰 관리")
                .pathsToMatch("/api/v1/admin/reviews/**")
                .build();
    }
}
