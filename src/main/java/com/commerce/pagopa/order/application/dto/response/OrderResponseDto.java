package com.commerce.pagopa.order.application.dto.response;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.orderitem.application.dto.response.OrderItemResponseDto;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import java.time.Instant;
import java.util.List;

public record OrderResponseDto(
        Long orderId,
        StatusResponseDto<OrderStatus> status,
        Instant orderedAt,
        Instant canceledAt,
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
