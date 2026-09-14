package com.commerce.pagopa.payment.application.dto.response;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.model.enums.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResult(
		Long paymentId,
		Long orderId,
		StatusResponseDto<PaymentStatus> status,
		String paymentMethod,
		Integer amount,
		LocalDateTime paidAt,
		LocalDateTime canceledAt
) {
	public static PaymentResult from(Payment payment) {
		return new PaymentResult(
				payment.getId(),
				payment.getOrder().getId(),
				StatusResponseDto.from(payment.getStatus()),
				payment.getPaymentMethod(),
				payment.getAmount(),
				payment.getPaidAt(),
				payment.getCanceledAt()
		);
	}
}
