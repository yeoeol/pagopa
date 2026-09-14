package com.commerce.pagopa.review.application.dto.response;

import com.commerce.pagopa.orderitem.application.dto.response.OrderItemResponseDto;
import com.commerce.pagopa.review.domain.model.Review;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponseDto(
        Long reviewId,
        String content,
        int rating,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        OrderItemResponseDto orderItem,
        List<ReviewImageResponseDto> reviewImages
) {
    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getContent(),
                review.getRating(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                OrderItemResponseDto.from(review.getOrderItem()),
                review.getImages().stream()
                        .map(ReviewImageResponseDto::from)
                        .toList()
        );
    }
}
