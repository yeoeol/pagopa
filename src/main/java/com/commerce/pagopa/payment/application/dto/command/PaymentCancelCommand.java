package com.commerce.pagopa.payment.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCancelCommand(
        @NotNull(message = "{validation.notNull}")
        Long paymentId,

        @NotBlank(message = "{validation.notBlank}")
        String cancelReason
) {
}
