package com.commerce.pagopa.order.application.dto.response;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponseDto(
        Long orderId,
        String orderNumber,
        String orderName,
        BigDecimal totalAmount,
        String status,
        UserResponseDto user,
        DeliveryResponseDto delivery,
        List<OrderProductResponseDto> orderProducts
) {
    public static OrderResponseDto from(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderName(),
                order.getTotalAmount(),
                order.getStatus().getDescription(),
                UserResponseDto.from(order.getUser()),
                DeliveryResponseDto.from(order.getDelivery()),
                order.getOrderProducts().stream()
                        .map(OrderProductResponseDto::from)
                        .toList()
        );
    }
}
