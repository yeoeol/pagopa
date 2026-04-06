package com.commerce.pagopa.domain.cart.dto.request;

public record CartAddRequestDto(
        Integer quantity,
        Long userId,
        Long productId
) {
}
