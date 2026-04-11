package com.commerce.pagopa.domain.review.dto.response;

import com.commerce.pagopa.domain.order.dto.response.OrderProductResponseDto;
import com.commerce.pagopa.domain.review.entity.Review;
import com.commerce.pagopa.domain.user.dto.response.UserResponseDto;

import java.util.List;

public record ReviewResponseDto(
        Long reviewId,
        int rating,
        String content,
        UserResponseDto user,
        OrderProductResponseDto orderProduct,
        List<ReviewImageResponseDto> images
) {
    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getRating(),
                review.getContent(),
                UserResponseDto.from(review.getUser()),
                OrderProductResponseDto.from(review.getOrderProduct()),
                review.getImages().stream()
                        .map(ReviewImageResponseDto::from)
                        .toList()
        );
    }
}
