package com.commerce.pagopa.domain.order.dto.response;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import com.commerce.pagopa.domain.order.entity.enums.PaymentMethod;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponseDto(
        Long orderId,
        String orderNumber,
        BigDecimal totalAmount,
        OrderStatus status,
        PaymentMethod paymentMethod,
        UserResponseDto user,
        DeliveryResponseDto delivery,
        List<OrderProductResponseDto> orderProducts
) {
    public static OrderResponseDto from(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentMethod(),
                UserResponseDto.from(order.getUser()),
                DeliveryResponseDto.from(order.getDelivery()),
                order.getOrderProducts().stream()
                        .map(OrderProductResponseDto::from)
                        .toList()
        );
    }
}
