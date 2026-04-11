package com.commerce.pagopa.domain.review.dto.response;

import com.commerce.pagopa.domain.review.entity.ReviewImage;

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
