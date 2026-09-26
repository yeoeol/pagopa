package com.commerce.pagopa.payment.application;

import com.commerce.pagopa.ordering.api.OrderPaymentApi;
import com.commerce.pagopa.ordering.api.OrderPaymentSummary;
import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCancellationRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCommand;
import com.commerce.pagopa.payment.application.dto.response.PaymentApprovalResponse;
import com.commerce.pagopa.payment.application.dto.response.PaymentCancellationResponse;
import com.commerce.pagopa.payment.application.dto.response.PaymentResult;
import com.commerce.pagopa.payment.domain.Payment;
import com.commerce.pagopa.payment.domain.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 결제 DB 트랜잭션 경계.
 * 외부 PG 호출은 포함하지 않고, 호출 전 검증/중간 상태 전환과 호출 후 확정만 담당한다.
 */
@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

	private static final String APPROVAL_IDEMPOTENCY_KEY_PREFIX = "payment-approval-";
	private static final String CANCELLATION_IDEMPOTENCY_KEY_PREFIX = "payment-cancellation-";

	private final OrderPaymentApi orderPaymentApi;
	private final PaymentRepository paymentRepository;

	@Transactional
	public PaymentResult request(Long userId, PaymentCommand command) {
		OrderPaymentSummary summary = orderPaymentApi.validateConfirmPayment(userId, command.orderId());

		return paymentRepository.findByOrderId(summary.orderId())
				.map(PaymentResult::from)
				.orElseGet(() -> PaymentResult.from(paymentRepository.save(
						Payment.create(
								command.paymentMethod(),
								summary.totalAmount(),
								summary.orderId()
						)
				)));
	}

	@Transactional
	public PaymentApprovalRequest prepareApproval(Long userId, Long paymentId) {
		Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);

		OrderPaymentSummary summary = orderPaymentApi.validateConfirmPayment(
				userId,
				payment.getOrderId()
		);

		payment.startApproval();
		return PaymentApprovalRequest.of(
				summary.orderId(),
				payment.getAmount(),
				payment.getPaymentMethod(),
				APPROVAL_IDEMPOTENCY_KEY_PREFIX + payment.getId()
		);
	}

	@Transactional
	public PaymentResult completeApproval(
			Long paymentId,
			PaymentApprovalResponse approval
	) {
		Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);

		boolean approved = payment.approve(
				approval.transactionId(),
				approval.approvedAmount(),
				approval.approvedAt()
		);
		if (approved) {
			orderPaymentApi.confirmPayment(payment.getOrderId(), approval.approvedAmount());
		}
		return PaymentResult.from(payment);
	}

	@Transactional
	public PaymentCancellationRequest prepareCancellation(Long userId, Long paymentId) {
		Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);
		orderPaymentApi.validateCancelAfterPayment(userId, payment.getOrderId());

		payment.startCancellation();
		return PaymentCancellationRequest.of(
				payment.getProviderTransactionId(),
				payment.getAmount(),
				CANCELLATION_IDEMPOTENCY_KEY_PREFIX + payment.getId()
		);
	}

	@Transactional
	public PaymentResult completeCancellation(
			Long paymentId,
			PaymentCancellationResponse cancellation
	) {
		Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);

		boolean canceled = payment.cancel(
				cancellation.transactionId(),
				cancellation.canceledAmount(),
				cancellation.canceledAt()
		);
		if (canceled) {
			orderPaymentApi.cancelAfterPayment(payment.getOrderId());
		}
		return PaymentResult.from(payment);
	}

	@Transactional
	public void markApprovalFailed(Long paymentId) {
		Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);
		payment.fail();
	}

	@Transactional
	public void revertCancellation(Long paymentId) {
		Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);
		payment.revertCancellation();
	}
}
