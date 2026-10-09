package com.commerce.pagopa.ordering.application.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record OrderCreateRequestDto(
        @Valid @NotNull(message = "{validation.notNull}") DeliveryRequestDto delivery,
        @NotEmpty(message = "{validation.notEmpty}") List<@Valid OrderItemRequestDto> products
) {
}
