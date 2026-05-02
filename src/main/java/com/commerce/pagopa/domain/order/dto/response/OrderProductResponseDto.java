package com.commerce.pagopa.domain.order.dto.response;

import com.commerce.pagopa.domain.order.entity.OrderProduct;
import com.commerce.pagopa.product.application.dto.response.ProductResponseDto;

import java.math.BigDecimal;

public record OrderProductResponseDto(
        Long orderProductId,
        int quantity,
        BigDecimal price,
        ProductResponseDto product
) {
    public static OrderProductResponseDto from(OrderProduct orderProduct) {
        return new OrderProductResponseDto(
                orderProduct.getId(),
                orderProduct.getQuantity(),
                orderProduct.getPrice(),
                ProductResponseDto.from(orderProduct.getProduct())
        );
    }
}
