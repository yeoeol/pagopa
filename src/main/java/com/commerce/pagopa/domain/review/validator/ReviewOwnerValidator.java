package com.commerce.pagopa.domain.review.validator;

import com.commerce.pagopa.domain.review.entity.Review;
import com.commerce.pagopa.domain.review.repository.ReviewRepository;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.global.validator.OwnerValidator;
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
