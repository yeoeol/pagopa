package com.commerce.pagopa.seller.order.application.dto.request;

import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusChangeRequestDto(
        @NotNull(message = "{validation.notNull}")
        OrderStatus status
) {
}
