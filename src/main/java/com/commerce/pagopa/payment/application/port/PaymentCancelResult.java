package com.commerce.pagopa.payment.application.port;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentCancelResult(
        boolean success,
        String status,              // "CANCELED" / "PARTIAL_CANCELED" 등
        BigDecimal cancelledAmount, // 이번 호출에서 취소된 금액 (Toss cancels[].cancelAmount)
        BigDecimal balanceAmount,   // 남은 환불 가능 잔액
        OffsetDateTime cancelledAt  // 이번 취소 시각
) {
}
