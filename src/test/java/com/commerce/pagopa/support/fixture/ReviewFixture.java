package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.ordering.domain.order.OrderItem;
import com.commerce.pagopa.review.domain.Review;

public final class ReviewFixture {

    private ReviewFixture() {
    }

    public static Review aReview(OrderItem orderItem) {
        return aReview(
                "좋아요",
                5,
                orderItem
        );
    }

    public static Review aReview(String content, Integer rating, OrderItem orderItem) {
        return Review.create(content, rating, orderItem);
    }
}
