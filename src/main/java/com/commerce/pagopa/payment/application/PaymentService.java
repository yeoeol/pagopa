package com.commerce.pagopa.payment.application;

import com.commerce.pagopa.order.application.OrderPaymentService;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.payment.application.dto.request.CancelPaymentCommand;
import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCancellationRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCommand;
import com.commerce.pagopa.payment.application.dto.response.PaymentApprovalResponse;
import com.commerce.pagopa.payment.application.dto.response.PaymentCancellationResponse;
import com.commerce.pagopa.payment.application.dto.response.PaymentResult;
import com.commerce.pagopa.payment.application.port.PaymentGateway;
import com.commerce.pagopa.payment.domain.model.Payment;
import com.commerce.pagopa.payment.domain.repository.PaymentRepository;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final OrderPaymentService orderPaymentService;
	private final PaymentRepository paymentRepository;
	private final PaymentGateway paymentGateway;

	@Transactional
	public PaymentResult pay(Long userId, PaymentCommand command) {
		Order order = orderPaymentService.getOrderForUpdate(userId, command.orderId());
		order.validateConfirmPayment();

		Payment payment = Payment.create(
				command.paymentMethod(),
				order.getTotalAmount(),
				order
		);
		paymentRepository.save(payment);

		payment.validateApprovable();
		PaymentApprovalResponse approval = paymentGateway.approve(
				PaymentApprovalRequest.of(
						order.getId(),
						payment.getAmount(),
						payment.getPaymentMethod()
				)
		);
		payment.approve(
				approval.transactionId(),
				approval.approvedAmount(),
				approval.approvedAt()
		);

		orderPaymentService.confirmPayment(order.getId(), approval.approvedAmount());
		return PaymentResult.from(payment);
	}

	@Transactional
	public PaymentResult cancel(Long userId, CancelPaymentCommand command) {
		Payment payment = paymentRepository.findByIdForUpdateOrThrow(command.paymentId());
		Order order = orderPaymentService.getOrderForUpdate(
				userId,
				payment.getOrder().getId()
		);
		order.validateCancelAfterPayment();

		payment.validateCancelable();
		PaymentCancellationResponse cancellation = paymentGateway.cancel(
				PaymentCancellationRequest.of(
						payment.getProviderTransactionId(),
						payment.getAmount()
				)
		);
		payment.cancel(
				cancellation.transactionId(),
				cancellation.canceledAmount(),
				cancellation.canceledAt()
		);

		orderPaymentService.cancelAfterPayment(order.getId());
		return PaymentResult.from(payment);
	}
}
