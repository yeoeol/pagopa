package com.commerce.pagopa.payment.application;

import java.math.BigDecimal;

public record PaymentCancelCommand(
        Long paymentId,
        String paymentKey,
        BigDecimal cancelAmount
) {
}
