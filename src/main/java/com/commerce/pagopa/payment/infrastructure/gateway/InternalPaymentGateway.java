package com.commerce.pagopa.payment.infrastructure.gateway;

import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCancellationRequest;
import com.commerce.pagopa.payment.application.dto.response.PaymentApprovalResponse;
import com.commerce.pagopa.payment.application.dto.response.PaymentCancellationResponse;
import com.commerce.pagopa.payment.application.port.PaymentGateway;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 서버 내부 테스트용 결제 서비스.
 * idempotencyKey 기준으로 승인/취소 결과를 보관해 조회·재시도를 지원한다.
 */
@Component
@Profile({"local", "test"})
public class InternalPaymentGateway implements PaymentGateway {

	private final ConcurrentHashMap<String, PaymentApprovalResponse> approvals = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, PaymentCancellationResponse> cancellations = new ConcurrentHashMap<>();

	@Override
	public PaymentApprovalResponse approve(PaymentApprovalRequest request) {
		return approvals.computeIfAbsent(
				request.idempotencyKey(),
				key -> PaymentApprovalResponse.of(
						"INTERNAL-" + key,
						request.amount(),
						Instant.now()
				)
		);
	}

	@Override
	public PaymentCancellationResponse cancel(PaymentCancellationRequest request) {
		return cancellations.computeIfAbsent(
				request.idempotencyKey(),
				key -> PaymentCancellationResponse.of(
						request.transactionId(),
						request.amount(),
						Instant.now()
				)
		);
	}

	@Override
	public Optional<PaymentApprovalResponse> findApprovalByIdempotencyKey(String idempotencyKey) {
		return Optional.ofNullable(approvals.get(idempotencyKey));
	}

	@Override
	public Optional<PaymentCancellationResponse> findCancellationByIdempotencyKey(String idempotencyKey) {
		return Optional.ofNullable(cancellations.get(idempotencyKey));
	}
}
