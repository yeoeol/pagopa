package com.commerce.pagopa.ordering.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.ordering.api.OrderItemApi;
import com.commerce.pagopa.ordering.api.OrderItemSummary;
import com.commerce.pagopa.ordering.domain.order.OrderItem;
import com.commerce.pagopa.ordering.domain.order.OrderItemRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemApiService implements OrderItemApi {

    private final OrderItemRepository orderItemRepository;

    @Override
    public OrderItemSummary getReviewableOrderItem(Long userId, Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findByIdOrThrow(orderItemId);
        orderItem.validateOrderUser(userId);

        return toSummary(orderItem);
    }

    private OrderItemSummary toSummary(OrderItem orderItem) {
        return new OrderItemSummary(orderItem.getId(), orderItem.getOrder()
                .getId(), orderItem.getProductId());
    }
}
