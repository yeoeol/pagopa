package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.order.domain.model.Address;
import com.commerce.pagopa.order.domain.model.Delivery;

public final class DeliveryFixture {

    private DeliveryFixture() {
    }

    public static Delivery aDelivery() {
        return aDelivery(AddressFixture.anAddress());
    }

    public static Delivery aDelivery(Address address) {
        return Delivery.create(address, "수령인", "01012345678", null);
    }
}
