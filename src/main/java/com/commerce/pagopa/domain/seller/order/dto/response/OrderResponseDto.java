package com.commerce.pagopa.domain.seller.order.dto.response;

import com.commerce.pagopa.domain.order.dto.response.DeliveryResponseDto;
import com.commerce.pagopa.domain.order.dto.response.OrderProductResponseDto;
import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.OrderProduct;
import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import com.commerce.pagopa.domain.order.entity.enums.PaymentMethod;
import com.commerce.pagopa.domain.user.dto.response.UserResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long orderId,
        String orderNumber,
        String orderName,
        BigDecimal totalAmount,
        OrderStatus status,
        PaymentMethod paymentMethod,
        LocalDateTime orderDate,
        UserResponseDto user,
        DeliveryResponseDto delivery,
        List<OrderProductResponseDto> orderProducts
) {
    public static OrderResponseDto from(Order order, Long sellerId) {
        List<OrderProduct> sellerProducts = order.getOrderProducts().stream()
                .filter(op -> op.getProduct().getSeller().getId().equals(sellerId))
                .toList();

        BigDecimal sellerTotal = sellerProducts.stream()
                .map(OrderProduct::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderName(),
                sellerTotal,
                order.getStatus(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                UserResponseDto.from(order.getUser()),
                DeliveryResponseDto.from(order.getDelivery()),
                sellerProducts.stream()
                        .map(OrderProductResponseDto::from)
                        .toList()
        );
    }
}
