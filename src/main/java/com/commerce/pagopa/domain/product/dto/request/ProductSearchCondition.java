package com.commerce.pagopa.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductSearchCondition(
        @NotBlank(message = "{validation.notBlank}")
        @Size(max = 100, message = "{validation.size}")
        String productName        // 상품명 검색
) {
}