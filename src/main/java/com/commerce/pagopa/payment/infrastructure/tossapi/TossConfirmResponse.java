package com.commerce.pagopa.payment.infrastructure.tossapi;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

record TossConfirmResponse(
        String status,           // "DONE" / "ABORTED" / "EXPIRED" / ...
        String paymentKey,
        BigDecimal totalAmount,
        OffsetDateTime approvedAt
) {
}
