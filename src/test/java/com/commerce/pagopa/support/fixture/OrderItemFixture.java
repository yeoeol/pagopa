package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderItem;

public final class OrderItemFixture {

    private OrderItemFixture() {
    }

    public static OrderItem anOrderItem(
            Product product,
            Order order
    ) {
        return anOrderItem(
                product,
                1,
                product.getPrice(),
                order
        );
    }

    private static OrderItem anOrderItem(
            Product product,
            int quantity,
            Integer price,
            Order order
    ) {
        return OrderItem.create(
                product.getName(),
                price,
                quantity,
                order,
                product
        );
    }
}
