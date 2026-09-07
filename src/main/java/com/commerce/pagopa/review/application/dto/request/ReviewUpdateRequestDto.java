package com.commerce.pagopa.review.application.dto.request;

import org.hibernate.validator.constraints.Range;

public record ReviewUpdateRequestDto(
        String content,

        @Range(min = 1, max = 5, message = "{validation.range}")
        Integer rating
) {
}
