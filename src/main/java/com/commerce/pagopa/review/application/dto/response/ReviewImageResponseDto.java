package com.commerce.pagopa.review.application.dto.response;

import com.commerce.pagopa.review.domain.model.ReviewImage;

public record ReviewImageResponseDto(
        String imageUrl,
        int displayOrder
) {
    public static ReviewImageResponseDto from(ReviewImage reviewImage) {
        return new ReviewImageResponseDto(
                reviewImage.getImageUrl(),
                reviewImage.getDisplayOrder()
        );
    }
}
