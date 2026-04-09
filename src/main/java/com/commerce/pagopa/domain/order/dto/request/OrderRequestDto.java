package com.commerce.pagopa.domain.order.dto.request;

import com.commerce.pagopa.domain.order.entity.enums.PaymentMethod;

import java.util.List;

public record OrderRequestDto(
        List<Long> cartIds,
        PaymentMethod paymentMethod
) {
}
