package com.commerce.pagopa.payment.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentApprovalCommand(
		@NotNull
		@Positive
		Long paymentId
) {
}
