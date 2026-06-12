package com.commerce.pagopa.order.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CartOrderRequestDto(
        @Valid
        @NotNull(message = "{validation.notNull}")
        DeliveryRequestDto delivery,

        @NotEmpty(message = "{validation.notEmpty}")
        List<@NotNull(message = "{validation.notNull}") @Positive(message = "{validation.min}") Long> cartIds
) {
}
