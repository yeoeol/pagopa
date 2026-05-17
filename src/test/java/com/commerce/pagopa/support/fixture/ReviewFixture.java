package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.review.domain.model.Review;
import com.commerce.pagopa.user.domain.model.User;

public final class ReviewFixture {

    private ReviewFixture() {
    }

    public static Review aReview(User user, OrderProduct orderProduct) {
        return Review.create(5, "좋아요", user, orderProduct);
    }

    public static Review aReview(User user, OrderProduct orderProduct, int rating, String content) {
        return Review.create(rating, content, user, orderProduct);
    }
}
