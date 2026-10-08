package com.commerce.pagopa.ordering.api;

public interface OrderItemApi {
    OrderItemSummary getReviewableOrderItem(Long userId, Long orderItemId);
}
