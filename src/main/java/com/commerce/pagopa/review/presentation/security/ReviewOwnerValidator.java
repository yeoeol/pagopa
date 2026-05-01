package com.commerce.pagopa.review.presentation.security;

import com.commerce.pagopa.global.validator.OwnerValidator;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.review.domain.model.Review;
import com.commerce.pagopa.review.domain.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Optional;

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
        return Optional.ofNullable(review.getUser())
                .map(User::getId)
                .orElse(null);
    }
}
