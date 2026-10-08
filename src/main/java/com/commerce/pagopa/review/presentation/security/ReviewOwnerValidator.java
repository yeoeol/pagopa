package com.commerce.pagopa.review.presentation.security;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.validator.OwnerValidator;
import com.commerce.pagopa.review.domain.Review;
import com.commerce.pagopa.review.domain.ReviewRepository;

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
        return review.getUserId();
    }
}
