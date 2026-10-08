package com.commerce.pagopa.review.application.dto.response;

import java.util.List;

import com.commerce.pagopa.identity.api.ReviewAuthorSummary;
import com.commerce.pagopa.review.domain.Review;

public record ProductReviewResponseDto(Long reviewId, int rating, String content, ReviewAuthorSummary author,
        List<ReviewImageResponseDto> images) {
    public static ProductReviewResponseDto from(Review review, ReviewAuthorSummary author) {
        return new ProductReviewResponseDto(review.getId(), review.getRating(), review.getContent(), author,
                review.getImages()
                        .stream()
                        .map(ReviewImageResponseDto::from)
                        .toList());
    }
}
