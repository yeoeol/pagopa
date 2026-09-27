package com.commerce.pagopa.ordering.application.dto.response;

import com.commerce.pagopa.ordering.domain.order.OrderItem;

public record OrderItemResponseDto(
        Long orderItemId,
        String productName,
        Integer orderPrice,
        Integer orderQuantity
) {
    public static OrderItemResponseDto from(
            OrderItem orderItem
    ) {
        return new OrderItemResponseDto(
                orderItem.getId(),
                orderItem.getProductName(),
                orderItem.getOrderPrice(),
                orderItem.getOrderQuantity()
        );
    }
}
