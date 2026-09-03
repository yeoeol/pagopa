package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.orderitem.domain.model.OrderItem;
import com.commerce.pagopa.product.domain.model.Product;

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
