package com.commerce.pagopa.order.application.dto.request;

import com.commerce.pagopa.order.domain.model.enums.OrderStatus;

public record OrderSearch(
        OrderStatus status
) {
}
