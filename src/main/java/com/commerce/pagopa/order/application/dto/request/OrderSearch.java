package com.commerce.pagopa.order.application.dto.request;

import com.commerce.pagopa.order.domain.model.enums.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrderSearch(
        OrderStatus status,
        Integer year
) {
    private static final int DEFAULT_RECENT_MONTHS = 6;

    // year 미지정 시 최근 6개월, 지정 시 해당 연도 시작 [start, end)
    public LocalDateTime start(LocalDateTime now) {
        return year == null
                ? now.minusMonths(DEFAULT_RECENT_MONTHS)
                : LocalDate.of(year, 1, 1).atStartOfDay();
    }

    public LocalDateTime end(LocalDateTime now) {
        return year == null
                ? now
                : LocalDate.of(year + 1, 1, 1).atStartOfDay();
    }
}
