package com.commerce.pagopa.payment.application.dto.response;

import java.time.Instant;

public record PaymentCancellationResponse(
		String transactionId,
		Integer canceledAmount,
		Instant canceledAt
) {
	public static PaymentCancellationResponse of(
			String transactionId,
			Integer canceledAmount,
			Instant canceledAt
	) {
		return new PaymentCancellationResponse(
				transactionId,
				canceledAmount,
				canceledAt
		);
	}
}
