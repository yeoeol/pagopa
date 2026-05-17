package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.product.domain.model.Product;

import java.math.BigDecimal;

public final class OrderProductFixture {

    private OrderProductFixture() {
    }

    public static OrderProduct anOrderProduct(Product product) {
        return OrderProduct.create(1, product.getPrice(), product);
    }

    public static OrderProduct anOrderProduct(Product product, int quantity, BigDecimal price) {
        return OrderProduct.create(quantity, price, product);
    }
}
