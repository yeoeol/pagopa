package com.commerce.pagopa.order.application.dto.request;

import com.commerce.pagopa.order.domain.model.enums.OrderStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public record OrderSearch(
        OrderStatus status,
        Integer year
) {
    private static final int DEFAULT_RECENT_MONTHS = 6;

    // year 미지정 시 최근 6개월, 지정 시 해당 연도 시작 [start, end)
    public Instant start(Instant now) {
        return year == null
                ? now.atZone(ZoneId.of("Asia/Seoul"))
                        .minusMonths(DEFAULT_RECENT_MONTHS)
                        .toInstant()
                : LocalDate.of(year, 1, 1)
                        .atStartOfDay(ZoneId.of("Asia/Seoul"))
                        .toInstant();
    }

    public Instant end(Instant now) {
        return year == null
                ? now
                : LocalDate.of(year + 1, 1, 1)
                        .atStartOfDay(ZoneId.of("Asia/Seoul"))
                        .toInstant();
    }
}
