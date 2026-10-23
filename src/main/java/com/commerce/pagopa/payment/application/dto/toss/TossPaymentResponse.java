package com.commerce.pagopa.payment.application.dto.toss;

import java.time.OffsetDateTime;

public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt,
        Cancels cancels,
        Integer totalAmount,
        Integer balanceAmount,
        String method
) {
    public record Cancels(
        String transactionKey,
        String cancelReason,
        OffsetDateTime canceledAt,
        Integer cancelAmount,
        String cancelStatus
    ) {}
}
