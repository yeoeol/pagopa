package com.commerce.pagopa.payment.application.dto.response;

import java.time.Instant;

public record PaymentApprovalResponse(
		String transactionId,
		Integer approvedAmount,
		Instant approvedAt
) {
	public static PaymentApprovalResponse of(
			String transactionId,
			Integer approvedAmount,
			Instant approvedAt
	) {
		return new PaymentApprovalResponse(
				transactionId,
				approvedAmount,
				approvedAt
		);
	}
}
