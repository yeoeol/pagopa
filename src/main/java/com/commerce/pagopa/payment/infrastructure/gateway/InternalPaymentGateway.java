package com.commerce.pagopa.payment.infrastructure.gateway;

import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCancellationRequest;
import com.commerce.pagopa.payment.application.dto.response.PaymentApprovalResponse;
import com.commerce.pagopa.payment.application.dto.response.PaymentCancellationResponse;
import com.commerce.pagopa.payment.application.port.PaymentGateway;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * 서버 내부 테스트용 결제 서비스
 * 모든 요청을 항상 승인하는 무상태 PG 시뮬레이터
 */
@Component
public class InternalPaymentGateway implements PaymentGateway {

	@Override
	public PaymentApprovalResponse approve(PaymentApprovalRequest request) {
		String transactionId = "INTERNAL-" + UUID.randomUUID();

		return PaymentApprovalResponse.of(
				transactionId,
				request.amount(),
				Instant.now()
		);
	}

	@Override
	public PaymentCancellationResponse cancel(PaymentCancellationRequest request) {
		return PaymentCancellationResponse.of(
				request.transactionId(),
				request.amount(),
				Instant.now()
		);
	}
}
