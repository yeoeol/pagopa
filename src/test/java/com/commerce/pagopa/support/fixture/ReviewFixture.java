package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.review.domain.Review;

public final class ReviewFixture {

    private ReviewFixture() {
    }

    public static Review aReview(Long orderItemId, Long userId) {
        return aReview(
                "좋아요",
                5,
                orderItemId,
                userId
        );
    }

    public static Review aReview(
            String content,
            Integer rating,
            Long orderItemId,
            Long userId
    ) {
        return Review.create(content, rating, orderItemId, userId);
    }
}
