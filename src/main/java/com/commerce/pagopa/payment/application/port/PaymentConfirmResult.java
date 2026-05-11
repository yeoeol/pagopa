package com.commerce.pagopa.payment.application.port;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentConfirmResult(
        boolean success,
        String status,            // Toss status 원문 ("DONE" 등) — 분기/로깅용
        String paymentKey,        // Toss 응답의 paymentKey (검증/대조)
        BigDecimal totalAmount,   // Toss 응답의 결제 총액
        OffsetDateTime approvedAt // 결제 승인 시각 (정산/로깅)
) {
}
