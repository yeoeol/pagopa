package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.user.domain.model.User;

public final class OrderFixture {

    private OrderFixture() {
    }

    public static Order anOrder(User buyer) {
        return Order.init(buyer, DeliveryFixture.aDelivery());
    }
}
