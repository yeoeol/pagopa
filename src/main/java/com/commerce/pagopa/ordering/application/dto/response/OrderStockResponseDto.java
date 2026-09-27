package com.commerce.pagopa.ordering.application.dto.response;

import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record OrderStockResponseDto(
        Long orderId,
        StatusResponseDto<OrderStatus> status,
        LocalDateTime orderedAt,
        LocalDateTime canceledAt,
        Long userId,
        List<OrderItemWithProductResponseDto> orderItems
) {
    public static OrderStockResponseDto from(
            Order order,
            Map<Long, ProductSummary> summary
    ) {
        return new OrderStockResponseDto(
                order.getId(),
                StatusResponseDto.from(order.getStatus()),
                order.getOrderedAt(),
                order.getCanceledAt(),
                order.getUserId(),
                order.getOrderItems().stream()
                        .map(oi -> OrderItemWithProductResponseDto.from(
                                oi, summary.get(oi.getProductId()))
                        )
                        .toList()
        );
    }
}
