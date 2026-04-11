package com.commerce.pagopa.domain.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record ReviewUpdateRequestDto(
        @NotNull(message = "{validation.notNull}")
        @Range(min = 1, max = 5, message = "{validation.range}")
        Integer rating,

        @NotBlank(message = "{validation.notBlank}")
        String content
) {
}
