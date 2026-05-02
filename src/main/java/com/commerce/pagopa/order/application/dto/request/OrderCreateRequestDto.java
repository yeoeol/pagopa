package com.commerce.pagopa.order.application.dto.request;

import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequestDto(
        @NotNull(message = "{validation.notNull}")
        PaymentMethod paymentMethod,

        @Valid
        @NotNull(message = "{validation.notNull}")
        DeliveryRequestDto delivery,

        @NotEmpty(message = "{validation.notEmpty}")
        List<@Valid OrderProductRequestDto> products
) {
}
