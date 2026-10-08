package com.commerce.pagopa.recommendation.presentation;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.recommendation.application.RecommendationQueryService;

@Tag(name = "RECOMMENDATION API", description = "개인화 상품 추천 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationQueryService recommendationQueryService;

    @Operation(summary = "추천 상품 조회", description = """
            사용자의 상품 및 검색어 관심도를 기준으로 판매 가능한 상품을 추천합니다.
            관심 상품이 부족하면 키워드 후보와 기본 상품 순서로 채웁니다.
            """)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductSummary>>> getRecommendations(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long userId,
            @Parameter(description = "조회할 추천 상품 개수", example = "10") @RequestParam(name = "limit", defaultValue = "10") @Min(value = 1, message = "{validation.min}") @Max(value = 100, message = "{validation.max}") Integer limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok(recommendationQueryService.getTopProducts(userId, limit)));
    }
}
