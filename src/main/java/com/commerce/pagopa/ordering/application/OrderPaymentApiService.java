package com.commerce.pagopa.ordering.application;

import com.commerce.pagopa.ordering.api.OrderPaymentApi;
import com.commerce.pagopa.ordering.api.OrderPaymentSummary;
import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderItem;
import com.commerce.pagopa.ordering.event.OrderConfirmed;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderPaymentApiService implements OrderPaymentApi {

	private final OrderPaymentService orderPaymentService;
	private final ApplicationEventPublisher events;

	@Override
	@Transactional(
			propagation = Propagation.MANDATORY
	)
	public OrderPaymentSummary validateConfirmPayment(
			Long userId,
			Long orderId
	) {
		Order order = orderPaymentService.getOrderForUpdateWithValidateOrdererId(userId, orderId);
		order.validateConfirmPayment();

		return new OrderPaymentSummary(
				order.getId(),
				order.getTotalAmount()
		);
	}

	@Override
	@Transactional(
			propagation = Propagation.MANDATORY
	)
	public void validateCancelAfterPayment(
			Long userId,
			Long orderId
	) {
		Order order = orderPaymentService.getOrderForUpdateWithValidateOrdererId(userId, orderId);
		order.validateCancelAfterPayment();
	}

	@Override
	@Transactional(
			propagation = Propagation.MANDATORY
	)
	public void confirmPayment(
			Long orderId,
			Integer approvedAmount
	) {
		Order order = orderPaymentService.getOrderForUpdate(orderId);
		order.confirmPayment(approvedAmount);

		events.publishEvent(new OrderConfirmed(
				UUID.randomUUID(),
				order.getId(),
				order.getUserId(),
				order.getOrderItems().stream()
						.map(OrderItem::getProductId)
						.distinct()
						.toList(),
				LocalDateTime.now()
		));
	}

	@Override
	@Transactional(
			propagation = Propagation.MANDATORY
	)
	public void cancelAfterPayment(Long orderId) {
		Order order = orderPaymentService.getOrderForUpdate(orderId);
		orderPaymentService.cancelAfterPayment(order.getId());
	}
}
