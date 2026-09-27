package com.commerce.pagopa.recommendation.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RecommendationProjectionRepository {
	boolean tryClaim(UUID eventId);

	void recordSearch(UUID eventId, Long userId, String keyword, LocalDateTime searchedAt);

	void recordPurchasedProducts(UUID eventId, Long userId, List<Long> productIds);
}
