package com.commerce.pagopa.review.application.dto.response;

import com.commerce.pagopa.orderitem.application.dto.response.OrderItemResponseDto;
import com.commerce.pagopa.review.domain.model.Review;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import java.util.List;

public record ReviewResponseDto(
        Long reviewId,
        int rating,
        String content,
        UserResponseDto user,
        OrderItemResponseDto orderProduct,
        List<ReviewImageResponseDto> images
) {
    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getRating(),
                review.getContent(),
                UserResponseDto.from(review.getUser()),
                OrderItemResponseDto.from(review.getOrderItem()),
                review.getImages().stream()
                        .map(ReviewImageResponseDto::from)
                        .toList()
        );
    }
}
