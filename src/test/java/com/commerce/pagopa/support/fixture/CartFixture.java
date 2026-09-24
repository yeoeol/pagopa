package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.basket.domain.Cart;

public final class CartFixture {

    private CartFixture() {
    }

    public static Cart aCart(Long userId) {
        return Cart.create(userId);
    }
}
