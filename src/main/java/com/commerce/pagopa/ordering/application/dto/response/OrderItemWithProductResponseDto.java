package com.commerce.pagopa.ordering.application.dto.response;

import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.ordering.domain.order.OrderItem;

public record OrderItemWithProductResponseDto(Long orderItemId, String productName, Integer orderPrice,
        Integer orderQuantity, ProductSummary product) {
    public static OrderItemWithProductResponseDto from(OrderItem orderItem, ProductSummary product) {
        return new OrderItemWithProductResponseDto(orderItem.getId(), orderItem.getProductName(),
                orderItem.getOrderPrice(), orderItem.getOrderQuantity(), product);
    }
}
