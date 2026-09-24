package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.global.entity.Address;
import com.commerce.pagopa.ordering.domain.delivery.Delivery;
import com.commerce.pagopa.ordering.domain.order.Order;

public final class DeliveryFixture {

    private DeliveryFixture() {
    }

    public static Delivery aDelivery(Long userId) {
        return aDelivery(
                AddressFixture.anAddress(),
                OrderFixture.anOrder(userId)
        );
    }

    public static Delivery aDelivery(Address address, Order order) {
        return Delivery.create(address, "요청사항", order);
    }
}
