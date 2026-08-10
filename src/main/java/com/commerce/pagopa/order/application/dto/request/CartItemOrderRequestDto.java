package com.commerce.pagopa.order.application.dto.request;

import com.commerce.pagopa.delivery.application.dto.request.DeliveryRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CartItemOrderRequestDto(
        @Valid
        @NotNull(message = "{validation.notNull}") DeliveryRequestDto delivery,

        @NotEmpty(message = "{validation.notEmpty}")
        List<@NotNull(message = "{validation.notNull}") @Positive(message = "{validation.min}") Long> cartItemIds
) {
}
