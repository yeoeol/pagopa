package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.ordering.domain.order.Order;

public final class OrderFixture {

    private OrderFixture() {
    }

    public static Order anOrder(Long buyerId) {
        return Order.init(buyerId);
    }
}
