package com.commerce.pagopa.domain.order.dto.response;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.enums.PaymentMethod;
import com.commerce.pagopa.domain.user.dto.response.UserResponseDto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponseDto(
        Long orderId,
        String orderNumber,
        BigDecimal totalAmount,
        String status,
        PaymentMethod paymentMethod,
        UserResponseDto user,
        List<OrderProductResponseDto> orderItems
) {
    public static OrderResponseDto from(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus().getDescription(),
                order.getPaymentMethod(),
                UserResponseDto.from(order.getUser()),
                order.getOrderProducts().stream()
                        .map(OrderProductResponseDto::from)
                        .toList()
        );
    }
}
