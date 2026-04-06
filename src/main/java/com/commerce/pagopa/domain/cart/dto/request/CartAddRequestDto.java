package com.commerce.pagopa.domain.cart.dto.request;

public record CartAddRequestDto(
        Long productId,
        int quantity
) {
}
