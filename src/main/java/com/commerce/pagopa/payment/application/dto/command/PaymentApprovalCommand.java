package com.commerce.pagopa.payment.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentApprovalCommand(
        @NotNull(message = "{validation.notNull}")
        Long paymentId,

        @NotBlank(message = "{validation.notBlank}")
        String providerPaymentId,

        @NotBlank(message = "{validation.notBlank}")
        String providerOrderId,

        @NotNull(message = "{validation.notNull}")
        @Positive(message = "{validation.min}")
        Integer amount
) {
}
