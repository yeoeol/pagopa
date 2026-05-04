package com.commerce.pagopa.admin.order.application.dto.response;

import com.commerce.pagopa.admin.user.application.dto.response.UserResponseDto;
import com.commerce.pagopa.order.application.dto.response.DeliveryResponseDto;
import com.commerce.pagopa.order.application.dto.response.OrderProductResponseDto;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponseDto(
        Long orderId,
        String orderNumber,
        String orderName,
        BigDecimal totalAmount,
        String status,
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
                order.getOrderName(),
                order.getTotalAmount(),
                order.getStatus().getDescription(),
                order.getPaymentMethod(),
                UserResponseDto.from(order.getUser()),
                DeliveryResponseDto.from(order.getDelivery()),
                flatProducts
        );
    }
}
