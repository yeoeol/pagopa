package com.commerce.pagopa.ordering.application.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemOrderRequestDto(
        @Valid @NotNull(message = "{validation.notNull}") DeliveryRequestDto delivery,
        @NotEmpty(
                message = "{validation.notEmpty}"
        ) List<@NotNull(message = "{validation.notNull}") @Positive(message = "{validation.min}") Long> cartItemIds
) {
}
