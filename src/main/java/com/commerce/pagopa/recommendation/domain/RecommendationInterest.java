package com.commerce.pagopa.recommendation.domain;

import java.time.LocalDateTime;

public record RecommendationInterest(
		Long userId,
		InterestType type,
		String interestKey,
		int score,
		LocalDateTime lastEventAt
) {
}
