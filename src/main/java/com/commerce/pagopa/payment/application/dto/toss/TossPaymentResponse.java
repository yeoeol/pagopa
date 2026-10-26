package com.commerce.pagopa.payment.application.dto.toss;

import java.time.OffsetDateTime;
import java.util.List;

public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt,
        List<Cancel> cancels,
        Integer totalAmount,
        Integer balanceAmount,
        String method
) {
    public record Cancel(
        String transactionKey,
        String cancelReason,
        OffsetDateTime canceledAt,
        Integer cancelAmount,
        String cancelStatus
    ) {}
}
