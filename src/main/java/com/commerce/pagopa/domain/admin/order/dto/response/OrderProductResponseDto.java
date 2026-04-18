package com.commerce.pagopa.domain.admin.order.dto.response;

import com.commerce.pagopa.domain.admin.product.dto.response.ProductResponseDto;
import com.commerce.pagopa.domain.order.entity.OrderProduct;

import java.math.BigDecimal;

public record OrderProductResponseDto(
        int quantity,
        BigDecimal price,
        ProductResponseDto product
) {
    public static OrderProductResponseDto from(OrderProduct orderProduct) {
        return new OrderProductResponseDto(
                orderProduct.getQuantity(),
                orderProduct.getPrice(),
                ProductResponseDto.from(orderProduct.getProduct())
        );
    }
}
