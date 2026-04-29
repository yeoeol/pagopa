package com.commerce.pagopa.domain.seller.order.service;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import com.commerce.pagopa.domain.seller.order.dto.request.OrderStatusChangeRequestDto;
import com.commerce.pagopa.domain.seller.order.dto.response.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerOrderService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> findAll(Long userId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllByUserId(userId, pageable);
        return orderPage.map(OrderResponseDto::from);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto find(Long orderId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        return OrderResponseDto.from(order);
    }

    @Transactional
    public void changeStatus(Long orderId, OrderStatusChangeRequestDto requestDto) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        OrderStatus status = requestDto.status();

        switch (status) {
            case PAID -> order.markAsPaid();
            case DELIVERING -> order.markAsDelivering();
            case COMPLETED -> order.markAsCompleted();
            case CANCELLED -> order.markAsCancelled();
        }
    }
}
