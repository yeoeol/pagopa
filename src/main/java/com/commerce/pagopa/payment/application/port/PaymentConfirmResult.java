package com.commerce.pagopa.payment.application.port;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentConfirmResult(
        boolean success,
        String status,            // PG 원본 상태 문자열 — 로깅용
        String paymentKey,        // PG 측 결제 식별자 (검증/대조)
        BigDecimal totalAmount,   // PG 응답의 결제 총액
        OffsetDateTime approvedAt // 결제 승인 시각 (정산/로깅)
) {
}
