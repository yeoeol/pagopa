package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.user.domain.model.User;

public final class OrderFixture {

    private OrderFixture() {
    }

    public static Order anOrder(User buyer) {
        return anOrder("ORD-1", buyer);
    }

    public static Order anOrder(String orderNumber, User buyer) {
        return Order.init(orderNumber, PaymentMethod.CARD, buyer, DeliveryFixture.aDelivery());
    }
}
