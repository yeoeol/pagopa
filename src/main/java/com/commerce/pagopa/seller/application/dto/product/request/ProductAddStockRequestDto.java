package com.commerce.pagopa.seller.application.dto.product.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductAddStockRequestDto(
		@NotNull(message = "{validation.notNull}")
		@PositiveOrZero(message = "{validation.min}")
		Integer quantity
) {
}
