package com.commerce.pagopa.order.application.dto.response;

import com.commerce.pagopa.order.domain.model.OrderProduct;

import java.math.BigDecimal;

public record OrderProductResponseDto(
        Long orderProductId,
        Long productId,
        String productName,
        int quantity,
        BigDecimal price
) {
    public static OrderProductResponseDto from(OrderProduct orderProduct) {
        return new OrderProductResponseDto(
                orderProduct.getOrderProductId(),
                orderProduct.getProductId(),
                orderProduct.getProductName(),
                orderProduct.getQuantity(),
                orderProduct.getPrice()
        );
    }
}
