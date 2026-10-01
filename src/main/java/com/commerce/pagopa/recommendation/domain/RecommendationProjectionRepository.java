package com.commerce.pagopa.recommendation.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface RecommendationProjectionRepository {

	boolean saveEventIfAbsent(RecommendationEvent event);

	void increaseInterest(
			Long userId,
			InterestType type,
			String interestKey,
			int weight,
			LocalDateTime occurredAt
	);

	List<RecommendationInterest> findTopInterests(
			Long userId,
			InterestType type,
			int limit
	);
}
