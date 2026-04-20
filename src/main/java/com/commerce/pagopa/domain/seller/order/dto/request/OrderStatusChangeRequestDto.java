package com.commerce.pagopa.domain.seller.order.dto.request;

import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusChangeRequestDto(
        @NotNull(message = "{validation.notNull}")
        OrderStatus status
) {
}
