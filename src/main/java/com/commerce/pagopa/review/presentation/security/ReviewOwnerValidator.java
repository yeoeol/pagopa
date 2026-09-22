package com.commerce.pagopa.review.presentation.security;

import com.commerce.pagopa.global.validator.OwnerValidator;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.review.domain.Review;
import com.commerce.pagopa.review.domain.ReviewRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Component("reviewOwnerValidator")
@RequiredArgsConstructor
public class ReviewOwnerValidator extends OwnerValidator<Review, Long> {

    private final ReviewRepository reviewRepository;

    @Override
    protected Optional<Review> findResource(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    @Override
    protected Long extractOwnerId(Review review) {
        return Optional.ofNullable(
                review.getOrderItem()
                        .getOrder()
                        .getUser()
                )
                .map(User::getId)
                .orElse(null);
    }
}
