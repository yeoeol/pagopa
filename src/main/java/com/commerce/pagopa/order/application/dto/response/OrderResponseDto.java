package com.commerce.pagopa.order.application.dto.response;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.orderitem.application.dto.response.OrderItemResponseDto;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import java.util.List;

public record OrderResponseDto(
        Long orderId,
        String orderNumber,
        String orderName,
        Integer totalAmount,
        OrderStatus status,
        UserResponseDto user,
        List<OrderItemResponseDto> orderProducts
) {
    public static OrderResponseDto from(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderName(),
                order.getTotalAmount(),
                order.getStatus(),
                UserResponseDto.from(order.getUser()),
                order.getOrderItems().stream()
                        .map(OrderItemResponseDto::from)
                        .toList()
        );
    }
}
