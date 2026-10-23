package com.commerce.pagopa.payment.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.commerce.pagopa.payment.domain.PaymentProvider;

public record PaymentRequestCommand(
        @NotNull(message = "{validation.notnull}")
        Long orderId,

        @NotBlank(message = "{validation.notblank}")
        @Size(min = 1, max = 50, message = "{validation.size}")
        String paymentMethod,

        @NotNull(message = "{validation.notnull}")
        PaymentProvider provider
) {
}
