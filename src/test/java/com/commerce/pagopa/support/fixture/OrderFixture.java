package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.user.domain.model.User;

import java.util.UUID;

public final class OrderFixture {

    private OrderFixture() {
    }

    public static Order anOrder(User buyer) {
        return anOrder("ORD-" + UUID.randomUUID(), buyer);
    }

    public static Order anOrder(String orderNumber, User buyer) {
        return Order.init(orderNumber, PaymentMethod.CARD, buyer, DeliveryFixture.aDelivery());
    }
}
