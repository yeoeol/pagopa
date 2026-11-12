package com.commerce.pagopa.ordering.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.ordering.api.OrderItemApi;
import com.commerce.pagopa.ordering.api.OrderItemSummary;
import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderItem;
import com.commerce.pagopa.ordering.domain.order.OrderRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemApiService implements OrderItemApi {

    private final OrderRepository orderRepository;

    @Override
    public OrderItemSummary getReviewableOrderItem(Long userId, Long orderItemId) {
        Order order = orderRepository.findByOrderItemIdWithItems(orderItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND));
        order.validateOwner(userId);

        return toSummary(order.getOrderItem(orderItemId));
    }

    private OrderItemSummary toSummary(OrderItem orderItem) {
        return new OrderItemSummary(
                orderItem.getId(),
                orderItem.getOrder()
                        .getId(),
                orderItem.getProductId()
        );
    }
}
