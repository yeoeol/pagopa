package com.commerce.pagopa.order.application;

import com.commerce.pagopa.payment.domain.model.Payment;

import java.math.BigDecimal;

public record OrderCancelCommand(
        Long orderId,
        Payment payment,
        BigDecimal cancelAmount
) {
}
