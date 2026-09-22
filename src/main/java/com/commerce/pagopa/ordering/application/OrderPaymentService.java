package com.commerce.pagopa.ordering.application;

import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.catalog.domain.ProductRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderItem;
import com.commerce.pagopa.ordering.domain.order.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;

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

		// 데드락 방지
		List<Long> productIds = order.getOrderItems().stream()
				.map(orderItem -> orderItem.getProduct().getId())
				.distinct()
				.sorted()
				.toList();

		Map<Long, Product> productMap = new HashMap<>();

		for (Long productId : productIds) {
			Product product = productRepository.findByIdForUpdateOrThrow(productId);
			productMap.put(productId, product);
		}

		// 주문 항목 수량만큼 재고 복구
		for (OrderItem op : order.getOrderItems()) {
			Product product = productMap.get(op.getProduct().getId());
			product.increaseStock(op.getOrderQuantity());
		}
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
		if (!order.getUser().getId().equals(userId)) {
			throw new BusinessException(ErrorCode.ORDER_NOT_MINE);
		}
	}
}
