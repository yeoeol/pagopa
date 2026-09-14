package com.commerce.pagopa.payment.application.dto.response;

import java.time.LocalDateTime;

public record PaymentApprovalResponse(
		String transactionId,
		Integer approvedAmount,
		LocalDateTime approvedAt
) {
	public static PaymentApprovalResponse of(
			String transactionId,
			Integer approvedAmount,
			LocalDateTime approvedAt
	) {
		return new PaymentApprovalResponse(
				transactionId,
				approvedAmount,
				approvedAt
		);
	}
}
