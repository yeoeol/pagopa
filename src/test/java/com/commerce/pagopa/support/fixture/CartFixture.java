package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.basket.domain.Cart;
import com.commerce.pagopa.identity.domain.User;

public final class CartFixture {

    private CartFixture() {
    }

    public static Cart aCart(User user) {
        return Cart.create(user);
    }
}
