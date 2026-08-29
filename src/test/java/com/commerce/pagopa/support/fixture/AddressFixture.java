package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.global.entity.Address;

public final class AddressFixture {

    private AddressFixture() {
    }

    public static Address anAddress() {
        return Address.create("01234", "서울특별시 강남구 테헤란로", "101번지 1");
    }
}
