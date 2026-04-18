package com.commerce.pagopa.domain.admin.product.dto.request;

import com.commerce.pagopa.domain.product.entity.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusChangeRequestDto(
        @NotNull(message = "{validation.notNull}")
        ProductStatus status
) {
}
