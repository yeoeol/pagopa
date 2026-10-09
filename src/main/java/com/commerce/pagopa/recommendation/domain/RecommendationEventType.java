package com.commerce.pagopa.recommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecommendationEventType {
    PRODUCT_SEARCHED("ProductSearched"), ORDER_CONFIRMED("OrderConfirmed"),
    ;

    private final String value;
}
