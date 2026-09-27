package com.commerce.pagopa.recommendation.application;

import com.commerce.pagopa.discovery.event.UserSearchRecorded;
import com.commerce.pagopa.ordering.event.OrderConfirmed;
import com.commerce.pagopa.recommendation.domain.RecommendationProjectionRepository;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class RecommendationEventListener {

	private final RecommendationProjectionRepository projections;

	@ApplicationModuleListener
	void on(UserSearchRecorded event) {
		if (!projections.tryClaim(event.eventId())) return;

		log.info("Received user search for {}", event.eventId());

		projections.recordSearch(
				event.eventId(),
				event.userId(),
				event.keyword(),
				event.searchedAt()
		);

		log.info("Finished user search for {}", event.eventId());
	}

	@ApplicationModuleListener
	void on(OrderConfirmed event) {
		if (!projections.tryClaim(event.eventId())) return;

		log.info("Received order confirm for {}", event.eventId());

		projections.recordPurchasedProducts(
				event.eventId(),
				event.userId(),
				event.productIds()
		);

		log.info("Finished order confirm for {}", event.eventId());
	}
}
