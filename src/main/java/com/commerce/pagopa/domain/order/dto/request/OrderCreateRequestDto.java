package com.commerce.pagopa.domain.order.dto.request;

import com.commerce.pagopa.domain.order.entity.enums.PaymentMethod;

import java.util.List;

public record OrderCreateRequestDto(
        String orderNumber,
        PaymentMethod paymentMethod,
        Long userId,
        List<OrderProductRequestDto> products
) {
}
