package com.commerce.pagopa.payment.application;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.payment.application.dto.request.PaymentApprovalRequest;
import com.commerce.pagopa.payment.application.dto.request.PaymentCommand;
import com.commerce.pagopa.payment.application.dto.response.PaymentApprovalResponse;
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

	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;
	private final PaymentGateway paymentGateway;

	@Transactional
	public PaymentResult pay(Long userId, PaymentCommand command) {
		Order order = orderRepository.findByIdOrThrow(command.orderId());
		validateOrdererId(userId, order);

		Payment payment = Payment.create(
				command.paymentMethod(),
				order.getTotalAmount(),
				order
		);
		paymentRepository.save(payment);

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

		order.confirmPayment(approval.approvedAmount());
		return PaymentResult.from(payment);
	}

	private static void validateOrdererId(
			Long userId,
			Order order
	) {
		if (!order.getUser().getId().equals(userId)) {
			throw new BusinessException(ErrorCode.ORDER_NOT_MINE);
		}
	}
}
