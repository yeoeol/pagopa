package com.commerce.pagopa.seller.order.application;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.seller.order.application.dto.request.OrderStatusChangeRequestDto;
import com.commerce.pagopa.seller.order.application.dto.response.OrderResponseDto;
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
    public Page<OrderResponseDto> findAll(Long sellerId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllBySellerId(sellerId, pageable);
        return orderPage.map(order -> OrderResponseDto.from(order, sellerId));
    }

    @Transactional(readOnly = true)
    public OrderResponseDto find(Long orderId, Long sellerId) {
        Order order = orderRepository.findByIdOrThrow(orderId);
        return OrderResponseDto.from(order, sellerId);
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
