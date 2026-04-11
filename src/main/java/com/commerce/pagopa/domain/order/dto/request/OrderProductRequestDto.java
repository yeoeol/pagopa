package com.commerce.pagopa.domain.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderProductRequestDto(
        @NotNull(message = "{validation.notNull}")
        Long productId,

        @NotNull(message = "{validation.notNull}")
        @Min(value = 1, message = "{validation.min}")
        Integer quantity
) {
}
