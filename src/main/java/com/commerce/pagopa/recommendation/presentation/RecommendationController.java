package com.commerce.pagopa.recommendation.presentation;

import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.recommendation.application.RecommendationQueryService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

	private final RecommendationQueryService recommendationQueryService;

	@GetMapping
	public ResponseEntity<ApiResponse<List<ProductSummary>>> getRecommendations(
			@AuthenticationPrincipal(expression = "userId") Long userId,
			@RequestParam(
					name = "limit",
					defaultValue = "10"
			)
			@Min(value = 1, message = "{validation.min}")
			@Max(value = 100, message = "{validation.max}")
			Integer limit
	) {
		return ResponseEntity.ok(
				ApiResponse.ok(recommendationQueryService.getTopProducts(
						userId,
						limit
				))
		);
	}
}
