package com.commerce.pagopa.seller.application.dto.product.request;

import jakarta.validation.constraints.*;

import java.util.List;

public record ProductRegisterRequestDto(
        @NotBlank(message = "{validation.notBlank}")
        String name,

        String description,

        @NotNull(message = "{validation.notNull}")
        @PositiveOrZero(message = "{validation.min}")
        Integer price,

        @NotNull(message = "{validation.notNull}")
        @Min(value = 0, message = "{validation.min}")
        Integer stockQuantity,

        @NotNull(message = "{validation.notNull}")
        Long categoryId,

        @NotEmpty(message = "{validation.notEmpty}")
        List<@NotBlank(message = "{validation.notBlank}")
                @Size(max = 512, message = "{validation.size}") String> imageUrls
) {
}
