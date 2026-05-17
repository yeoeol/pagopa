package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.user.domain.model.User;

public final class CartFixture {

    private CartFixture() {
    }

    public static Cart aCart(User user, Product product) {
        return Cart.create(1, user, product);
    }

    public static Cart aCart(User user, Product product, int quantity) {
        return Cart.create(quantity, user, product);
    }
}
