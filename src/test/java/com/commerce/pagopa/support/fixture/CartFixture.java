package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.user.domain.model.User;

public final class CartFixture {

    private CartFixture() {
    }

    public static Cart aCart(User user) {
        return Cart.create(user);
    }
}
