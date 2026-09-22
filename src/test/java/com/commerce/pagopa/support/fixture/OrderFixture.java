package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.ordering.domain.order.Order;

public final class OrderFixture {

    private OrderFixture() {
    }

    public static Order anOrder(User buyer) {
        return Order.init(buyer);
    }
}
