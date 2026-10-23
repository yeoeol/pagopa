package com.commerce.pagopa.payment.application.dto.toss;

public record TossConfirmRequest(
        String paymentKey,
        String orderId,
        Integer amount
) {
}
