package com.commerce.pagopa.recommendation.infrastructure;

import com.commerce.pagopa.recommendation.domain.RecommendationProjectionRepository;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRecommendationProjectionRepository implements RecommendationProjectionRepository {

	private final Map<UUID, String> repository = new ConcurrentHashMap<>();
	private final JsonMapper jsonMapper = new JsonMapper();

	@Override
	public boolean tryClaim(UUID eventId) {
		return !repository.containsKey(eventId);
	}

	@Override
	public void recordSearch(
			UUID eventId,
			Long userId,
			String keyword,
			LocalDateTime searchedAt
	) {
		repository.put(
				eventId,
				jsonMapper.writeValueAsString(new Search(
						userId,
						keyword,
						searchedAt
				))
		);
	}

	@Override
	public void recordPurchasedProducts(
			UUID eventId,
			Long userId,
			List<Long> productIds
	) {
		repository.put(
				eventId,
				jsonMapper.writeValueAsString(productIds)
		);
	}

	record Search(
			Long userId,
			String keyword,
			LocalDateTime searchedAt
	) {}
}
