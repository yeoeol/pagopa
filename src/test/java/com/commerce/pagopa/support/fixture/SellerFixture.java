package com.commerce.pagopa.support.fixture;

import java.time.LocalDateTime;

import com.commerce.pagopa.merchant.domain.Seller;

public final class SellerFixture {

    public SellerFixture() {
    }

    public static Seller aSeller(Long userId) {
        return Seller.create(
                userId,
                LocalDateTime.now()
        );
    }
}
