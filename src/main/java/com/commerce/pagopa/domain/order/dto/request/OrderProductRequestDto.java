package com.commerce.pagopa.domain.order.dto.request;

public record OrderProductRequestDto(
        int quantity,
        Long productId
) {
}
