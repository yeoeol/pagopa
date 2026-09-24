package com.commerce.pagopa.ordering.application;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

	private final OrderRepository orderRepository;
	private final OrderService orderService;

	@Transactional
	public Order getOrderForUpdateWithValidateOrdererId(Long userId, Long orderId) {
		Order order = orderRepository.findByIdForUpdateOrThrow(orderId);
		validateOrdererId(userId, order);
		return order;
	}

	@Transactional
	public Order getOrderForUpdate(Long orderId) {
		return orderRepository.findByIdForUpdateOrThrow(orderId);
	}

	@Transactional
	public void cancelAfterPayment(Long orderId) {
		// 주문 존재 여부 확인
		Order order = orderRepository.findByIdForUpdateOrThrow(orderId);
		order.cancelAfterPayment(LocalDateTime.now());

		orderService.cancelOrder(orderId);
	}

	@Transactional
	public void confirmPayment(Long orderId, Integer amount) {
		Order order = orderRepository.findByIdOrThrow(orderId);
		order.confirmPayment(amount);
	}

	public void validateOrdererId(
			Long userId,
			Order order
	) {
		if (!order.getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.ORDER_NOT_MINE);
		}
	}
}
