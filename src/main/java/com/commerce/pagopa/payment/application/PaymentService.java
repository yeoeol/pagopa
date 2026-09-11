package com.commerce.pagopa.payment.application;

import com.commerce.pagopa.payment.application.dto.request.*;
import com.commerce.pagopa.payment.application.dto.response.PaymentApprovalResponse;
import com.commerce.pagopa.payment.application.dto.response.PaymentCancellationResponse;
import com.commerce.pagopa.payment.application.dto.response.PaymentResult;
import com.commerce.pagopa.payment.application.exception.PaymentGatewayRejectedException;
import com.commerce.pagopa.payment.application.port.PaymentGateway;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 결제 유스케이스 오케스트레이션.
 * DB 트랜잭션(사전 검증·중간/최종 상태)과 외부 PG 호출을 분리한다.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentTransactionService paymentTransactionService;
	private final PaymentGateway paymentGateway;

	public PaymentResult request(Long userId, PaymentCommand command) {
		return paymentTransactionService.request(userId, command);
	}

	public PaymentResult approve(Long userId, PaymentApprovalCommand command) {
		Long paymentId = command.paymentId();
		PaymentApprovalRequest request =
				paymentTransactionService.prepareApproval(userId, paymentId);

		try {
			PaymentApprovalResponse approval = paymentGateway
					.findApprovalByIdempotencyKey(request.idempotencyKey())
					.orElseGet(() -> paymentGateway.approve(request));

			return paymentTransactionService.completeApproval(paymentId, approval);
		} catch (PaymentGatewayRejectedException e) {
			paymentTransactionService.markApprovalFailed(paymentId);
			throw e;
		}
	}

	public PaymentResult cancel(Long userId, CancelPaymentCommand command) {
		Long paymentId = command.paymentId();
		PaymentCancellationRequest request =
				paymentTransactionService.prepareCancellation(userId, paymentId);

		try {
			PaymentCancellationResponse cancellation = paymentGateway
					.findCancellationByIdempotencyKey(request.idempotencyKey())
					.orElseGet(() -> paymentGateway.cancel(request));

			return paymentTransactionService.completeCancellation(paymentId, cancellation);
		} catch (PaymentGatewayRejectedException e) {
			paymentTransactionService.revertCancellation(paymentId);
			throw e;
		}
	}
}
