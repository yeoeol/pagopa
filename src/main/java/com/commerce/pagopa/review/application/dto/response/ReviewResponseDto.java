package com.commerce.pagopa.review.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.commerce.pagopa.review.domain.Review;

public record ReviewResponseDto(Long reviewId, Long authorId, String content, int rating, LocalDateTime createdAt,
        LocalDateTime updatedAt, Long orderItemId, List<ReviewImageResponseDto> reviewImages) {
    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(review.getId(), review.getUserId(), review.getContent(), review.getRating(),
                review.getCreatedAt(), review.getUpdatedAt(), review.getOrderItemId(), review.getImages()
                        .stream()
                        .map(ReviewImageResponseDto::from)
                        .toList());
    }
}
