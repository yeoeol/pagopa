package com.commerce.pagopa.payment.infrastructure.tossapi;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

record TossCancelResponse(
        String status,            // "CANCELED" / "PARTIAL_CANCELED" / ...
        BigDecimal balanceAmount,
        List<Cancel> cancels      // 취소 이력 배열, 마지막 원소가 최근 취소
) {
    record Cancel(
            BigDecimal cancelAmount,
            OffsetDateTime canceledAt
    ) {
    }
}
