package com.commerce.pagopa.review.application.dto.response;

import com.commerce.pagopa.review.domain.model.Review;

import java.util.List;

public record ProductReviewResponseDto(
        Long reviewId,
        int rating,
        String content,
        ReviewAuthorResponseDto user,
        List<ReviewImageResponseDto> images
) {
    public static ProductReviewResponseDto from(Review review) {
        return new ProductReviewResponseDto(
                review.getId(),
                review.getRating(),
                review.getContent(),
                ReviewAuthorResponseDto.from(review.getUser()),
                review.getImages().stream()
                        .map(ReviewImageResponseDto::from)
                        .toList()
        );
    }
}
