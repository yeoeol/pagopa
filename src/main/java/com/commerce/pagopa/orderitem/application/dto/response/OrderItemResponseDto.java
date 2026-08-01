package com.commerce.pagopa.orderitem.application.dto.response;

import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.orderitem.domain.model.OrderItem;
import com.commerce.pagopa.product.application.dto.response.ProductResponseDto;

public record OrderItemResponseDto(
        Long orderItemId,
        String productName,
        Integer orderPrice,
        Integer orderQuantity,
        OrderResponseDto order,
        ProductResponseDto product
) {
    public static OrderItemResponseDto from(OrderItem orderItem) {
        return new OrderItemResponseDto(
                orderItem.getId(),
                orderItem.getProductName(),
                orderItem.getOrderPrice(),
                orderItem.getOrderQuantity(),
                OrderResponseDto.from(orderItem.getOrder()),
                ProductResponseDto.from(orderItem.getProduct())
        );
    }
}
