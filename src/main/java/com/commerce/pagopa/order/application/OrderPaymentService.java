package com.commerce.pagopa.order.application;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.orderitem.domain.model.OrderItem;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
	public Order getOrderForUpdate(Long userId, Long orderId) {
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
		order.cancelAfterPayment(Instant.now());

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
