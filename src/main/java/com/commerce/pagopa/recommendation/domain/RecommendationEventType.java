package com.commerce.pagopa.recommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecommendationEventType {
	USER_SEARCH_RECORDED("UserSearchRecorded"),
	ORDER_CONFIRMED("OrderConfirmed");

	private final String value;
}
