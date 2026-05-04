package com.commerce.pagopa.seller.order.application.dto.response;

import com.commerce.pagopa.order.application.dto.response.DeliveryResponseDto;
import com.commerce.pagopa.order.application.dto.response.OrderProductResponseDto;
import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SellerOrderResponseDto(
        Long orderId,
        Long sellerOrderId,
        String orderNumber,
        String sellerOrderNumber,
        String orderName,
        BigDecimal totalAmount,
        SellerOrderStatus status,
        PaymentMethod paymentMethod,
        LocalDateTime orderDate,
        UserResponseDto user,
        DeliveryResponseDto delivery,
        List<OrderProductResponseDto> orderProducts
) {
    public static SellerOrderResponseDto from(SellerOrder sellerOrder) {
        return new SellerOrderResponseDto(
                sellerOrder.getOrder().getId(),
                sellerOrder.getId(),
                sellerOrder.getOrder().getOrderNumber(),
                sellerOrder.getSellerOrderNumber(),
                sellerOrder.getOrder().getOrderName(),
                sellerOrder.getSellerTotalAmount(),
                sellerOrder.getStatus(),
                sellerOrder.getOrder().getPaymentMethod(),
                sellerOrder.getOrder().getCreatedAt(),
                UserResponseDto.from(sellerOrder.getOrder().getUser()),
                DeliveryResponseDto.from(sellerOrder.getOrder().getDelivery()),
                sellerOrder.getOrderProducts().stream()
                        .map(OrderProductResponseDto::from)
                        .toList()
        );
    }
}
