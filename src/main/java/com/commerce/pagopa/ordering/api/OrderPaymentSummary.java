package com.commerce.pagopa.ordering.api;

public record OrderPaymentSummary(
        Long orderId,
        Integer totalAmount
) {
}
