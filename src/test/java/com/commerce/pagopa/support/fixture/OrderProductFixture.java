package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.orderitem.domain.model.OrderItem;
import com.commerce.pagopa.product.domain.model.Product;

import java.math.BigDecimal;

public final class OrderProductFixture {

    private OrderProductFixture() {
    }

    public static OrderItem anOrderProduct(Product product) {
        return anOrderProduct(product, 1, product.getPrice());
    }

    public static OrderItem anOrderProduct(Product product, int quantity, BigDecimal price) {
        return OrderItem.create(product.getId(), product.getName(), quantity, price);
    }
}
