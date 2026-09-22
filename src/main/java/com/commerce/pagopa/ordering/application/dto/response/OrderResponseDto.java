package com.commerce.pagopa.ordering.application.dto.response;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.identity.application.dto.response.UserResponseDto;
import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long orderId,
        StatusResponseDto<OrderStatus> status,
        LocalDateTime orderedAt,
        LocalDateTime canceledAt,
        UserResponseDto user,
        List<OrderItemResponseDto> orderItems
) {
    public static OrderResponseDto from(Order order) {
        return new OrderResponseDto(
                order.getId(),
                StatusResponseDto.from(order.getStatus()),
                order.getOrderedAt(),
                order.getCanceledAt(),
                UserResponseDto.from(order.getUser()),
                order.getOrderItems().stream()
                        .map(OrderItemResponseDto::from)
                        .toList()
        );
    }
}
