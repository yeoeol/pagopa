package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.delivery.domain.model.Delivery;
import com.commerce.pagopa.global.entity.Address;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.user.domain.model.User;

public final class DeliveryFixture {

    private DeliveryFixture() {
    }

    public static Delivery aDelivery() {
        User user = UserFixture.aUser("test");

        return aDelivery(
                AddressFixture.anAddress(),
                OrderFixture.anOrder(user)
        );
    }

    public static Delivery aDelivery(Address address, Order order) {
        return Delivery.create(address, "요청사항", order);
    }
}
