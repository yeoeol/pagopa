package com.commerce.pagopa.review.application.dto.request;

import org.hibernate.validator.constraints.Range;

public record ReviewUpdateRequestDto(
        @Range(min = 1, max = 5, message = "{validation.range}")
        Integer rating,

        String content
) {
}
