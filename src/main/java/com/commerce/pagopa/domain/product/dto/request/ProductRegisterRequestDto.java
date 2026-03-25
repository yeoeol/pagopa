package com.commerce.pagopa.domain.product.dto.request;

import java.math.BigDecimal;

public record ProductRegisterRequestDto(
        String name,
        String description,
        BigDecimal price,
        int stock,
        Long categoryId
) {
}
