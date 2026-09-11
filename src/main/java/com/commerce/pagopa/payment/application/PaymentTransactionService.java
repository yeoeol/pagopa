package com.commerce.pagopa.payment.application;

import com.commerce.pagopa.order.application.OrderPaymentService;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCancellationRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCommand;
import com.commerce.pagopa.payment.application.dto.response.PaymentApprovalResponse;
import com.commerce.pagopa.payment.application.dto.response.PaymentCancellationResponse;
import com.commerce.pagopa.payment.application.dto.response.PaymentResult;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;

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

	private final OrderPaymentService orderPaymentService;
	private final PaymentRepository paymentRepository;

	@Transactional
	public PaymentResult request(Long userId, PaymentCommand command) {
		Order order = orderPaymentService.getOrderForUpdate(userId, command.orderId());
		order.validateConfirmPayment();

		return paymentRepository.findByOrderId(order.getId())
				.map(PaymentResult::from)
				.orElseGet(() -> PaymentResult.from(paymentRepository.save(
						Payment.create(command.paymentMethod(), order.getTotalAmount(), order)
				)));
	}

	@Transactional
	public PaymentApprovalRequest prepareApproval(Long userId, Long paymentId) {
		Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);
		Order order = orderPaymentService.getOrderForUpdate(userId, payment.getOrder().getId());
		order.validateConfirmPayment();

		payment.startApproval();
		return PaymentApprovalRequest.of(
				order.getId(),
				payment.getAmount(),
				payment.getPaymentMethod(),
				"payment-approval-" + payment.getId()
		);
	}

	@Transactional
	public PaymentResult completeApproval(
			Long paymentId,
			PaymentApprovalResponse approval
	) {
		Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);
		Order order = orderPaymentService.getOrderForUpdate(payment.getOrder().getId());

		payment.approve(
				approval.transactionId(),
				approval.approvedAmount(),
				approval.approvedAt()
		);
		orderPaymentService.confirmPayment(order.getId(), approval.approvedAmount());
		return PaymentResult.from(payment);
	}

	@Transactional
	public PaymentCancellationRequest prepareCancellation(Long userId, Long paymentId) {
		Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);
		Order order = orderPaymentService.getOrderForUpdate(userId, payment.getOrder().getId());
		order.validateCancelAfterPayment();

		payment.startCancellation();
		return PaymentCancellationRequest.of(
				payment.getProviderTransactionId(),
				payment.getAmount(),
				"payment-cancellation-" + payment.getId()
		);
	}

	@Transactional
	public PaymentResult completeCancellation(
			Long paymentId,
			PaymentCancellationResponse cancellation
	) {
		Payment payment = paymentRepository.findByIdForUpdateOrThrow(paymentId);
		orderPaymentService.getOrderForUpdate(payment.getOrder().getId());

		payment.cancel(
				cancellation.transactionId(),
				cancellation.canceledAmount(),
				cancellation.canceledAt()
		);
		orderPaymentService.cancelAfterPayment(payment.getOrder().getId());
		return PaymentResult.from(payment);
	}
}
