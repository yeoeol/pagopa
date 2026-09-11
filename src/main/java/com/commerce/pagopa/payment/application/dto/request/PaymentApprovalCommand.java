package com.commerce.pagopa.payment.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record PaymentApprovalCommand(
		@NotNull(message = "{validation.notNull}")
		Long paymentId
) {
}
