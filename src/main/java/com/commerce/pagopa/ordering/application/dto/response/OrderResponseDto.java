package com.commerce.pagopa.ordering.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderStatus;

public record OrderResponseDto(Long orderId, StatusResponseDto<OrderStatus> status, LocalDateTime orderedAt,
        LocalDateTime canceledAt, Long userId, List<OrderItemResponseDto> orderItems) {
    public static OrderResponseDto from(Order order) {
        return new OrderResponseDto(order.getId(), StatusResponseDto.from(order.getStatus()), order.getOrderedAt(),
                order.getCanceledAt(), order.getUserId(), order.getOrderItems()
                        .stream()
                        .map(OrderItemResponseDto::from)
                        .toList());
    }
}
