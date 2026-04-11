package com.commerce.pagopa.domain.review.dto.request;

public record ReviewUpdateRequestDto(
        int rating,
        String content
) {
}
