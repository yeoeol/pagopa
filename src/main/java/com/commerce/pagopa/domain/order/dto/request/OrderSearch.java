package com.commerce.pagopa.domain.order.dto.request;

import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;

public record OrderSearch(
        OrderStatus status
) {
}
