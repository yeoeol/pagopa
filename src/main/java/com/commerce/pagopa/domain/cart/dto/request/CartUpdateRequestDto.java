package com.commerce.pagopa.domain.cart.dto.request;

public record CartUpdateRequestDto(
        Long productId,
        boolean isAdd
) {
}
