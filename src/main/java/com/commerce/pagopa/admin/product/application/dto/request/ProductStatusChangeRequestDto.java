package com.commerce.pagopa.admin.product.application.dto.request;

import com.commerce.pagopa.product.domain.model.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusChangeRequestDto(
        @NotNull(message = "{validation.notNull}")
        ProductStatus status
) {
}
