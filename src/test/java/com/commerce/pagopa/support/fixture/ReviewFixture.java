package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.orderitem.domain.model.OrderItem;
import com.commerce.pagopa.review.domain.model.Review;
import com.commerce.pagopa.user.domain.model.User;

public final class ReviewFixture {

    private ReviewFixture() {
    }

    public static Review aReview(User user, OrderItem orderItem) {
        return Review.create(5, "좋아요", user, orderItem);
    }

    public static Review aReview(User user, OrderItem orderItem, int rating, String content) {
        return Review.create(rating, content, user, orderItem);
    }
}
