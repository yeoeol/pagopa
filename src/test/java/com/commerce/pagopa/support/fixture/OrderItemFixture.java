package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderItem;

public final class OrderItemFixture {

    private OrderItemFixture() {
    }

    public static OrderItem anOrderItem(
            String productName,
            Integer price,
            Long productId,
            Order order
    ) {
        return anOrderItem(
                productName,
                productId,
                1,
                price,
                order
        );
    }

    private static OrderItem anOrderItem(
            String productName,
            Long productId,
            int quantity,
            Integer price,
            Order order
    ) {
        return OrderItem.create(
                productName,
                price,
                quantity,
                order,
                productId
        );
    }
}
