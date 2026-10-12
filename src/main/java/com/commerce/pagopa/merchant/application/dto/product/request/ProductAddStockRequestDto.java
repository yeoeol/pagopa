package com.commerce.pagopa.merchant.application.dto.product.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductAddStockRequestDto(
        @NotNull(message = "{validation.notNull}") @Positive(message = "{validation.min}") Integer quantity
) {
}
