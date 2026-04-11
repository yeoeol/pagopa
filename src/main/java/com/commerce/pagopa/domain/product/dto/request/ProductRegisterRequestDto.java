package com.commerce.pagopa.domain.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ProductRegisterRequestDto(
        @NotBlank(message = "{validation.notBlank}")
        String name,

        @NotBlank(message = "{validation.notBlank}")
        String description,

        @NotNull(message = "{validation.notNull}")
        @DecimalMin(value = "0.0", inclusive = false, message = "{validation.min}")
        BigDecimal price,

        @NotNull(message = "{validation.notNull}")
        @Min(value = 0, message = "{validation.min}")
        Integer stock,

        @NotNull(message = "{validation.notNull}")
        Long categoryId,

        @NotNull(message = "{validation.notNull}")
        List<String> imageUrls
) {
}
