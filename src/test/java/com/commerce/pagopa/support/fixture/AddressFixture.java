package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.order.domain.model.Address;

public final class AddressFixture {

    private AddressFixture() {
    }

    public static Address anAddress() {
        return new Address("12345", "서울시 강남구", "101동 101호");
    }
}
