package com.commerce.pagopa.order.application.dto.response;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
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
        // 클라이언트는 SellerOrder 구분 없이 평탄화된 상품 목록 조회
        List<OrderProductResponseDto> flatProducts = order.getSellerOrders().stream()
                .flatMap(so -> so.getOrderProducts().stream())
                .map(OrderProductResponseDto::from)
                .toList();

        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentMethod(),
                UserResponseDto.from(order.getUser()),
                DeliveryResponseDto.from(order.getDelivery()),
                flatProducts
        );
    }
}
