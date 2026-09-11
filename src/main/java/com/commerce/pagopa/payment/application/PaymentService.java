package com.commerce.pagopa.payment.application;

import com.commerce.pagopa.payment.application.dto.request.*;
import com.commerce.pagopa.payment.application.dto.response.PaymentResult;
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
		PaymentApprovalRequest request =
				paymentTransactionService.prepareApproval(userId, command.paymentId());

		return paymentTransactionService.completeApproval(
				command.paymentId(),
				paymentGateway.approve(request)
		);
	}

	public PaymentResult cancel(Long userId, CancelPaymentCommand command) {
		PaymentCancellationRequest request =
				paymentTransactionService.prepareCancellation(userId, command.paymentId());

		return paymentTransactionService.completeCancellation(
				command.paymentId(),
				paymentGateway.cancel(request)
		);
	}
}
