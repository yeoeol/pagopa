package com.commerce.pagopa.domain.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

import java.util.List;

public record ReviewCreateRequestDto(
        @NotNull(message = "{validation.notNull}")
        @Range(min = 1, max = 5, message = "{validation.range}")
        Integer rating,     // 1 ~ 5

        @NotBlank(message = "{validation.notBlank}")
        String content,

        @NotNull(message = "{validation.notNull}")
        Long orderProductId,

        @NotNull(message = "{validation.notNull}")
        List<String> imageUrls
) {
}
