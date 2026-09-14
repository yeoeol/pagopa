package com.commerce.pagopa.payment.application.dto.response;

import java.time.LocalDateTime;

public record PaymentCancellationResponse(
		String transactionId,
		Integer canceledAmount,
		LocalDateTime canceledAt
) {
	public static PaymentCancellationResponse of(
			String transactionId,
			Integer canceledAmount,
			LocalDateTime canceledAt
	) {
		return new PaymentCancellationResponse(
				transactionId,
				canceledAmount,
				canceledAt
		);
	}
}
