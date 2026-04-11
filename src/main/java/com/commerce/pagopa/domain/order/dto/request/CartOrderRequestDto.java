package com.commerce.pagopa.domain.order.dto.request;

import com.commerce.pagopa.domain.order.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CartOrderRequestDto(
        @NotNull(message = "{validation.notNull}")
        PaymentMethod paymentMethod,

        @NotEmpty(message = "{validation.notEmpty}")
        List<@NotNull(message = "{validation.notNull}") @Positive(message = "{validation.min}") Long> cartIds
) {
}
