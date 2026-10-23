package com.commerce.pagopa.payment.application.dto.command;

import jakarta.validation.constraints.NotNull;

public record PaymentApprovalCommand(
        @NotNull(message = "{validation.notNull}")
        Long paymentId
) {
}
