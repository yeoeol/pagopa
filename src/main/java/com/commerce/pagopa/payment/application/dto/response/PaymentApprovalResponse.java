package com.commerce.pagopa.payment.application.dto.response;

import java.time.Instant;

public record PaymentApprovalResponse(
		String transactionId,
		Integer approvedAmount,
		Instant approvedAt
) {
}
