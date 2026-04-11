package com.commerce.pagopa.domain.review.validator;

import com.commerce.pagopa.domain.review.entity.Review;
import com.commerce.pagopa.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("productOwnerValidator")
@RequiredArgsConstructor
public class ReviewOwnerValidator {

    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public boolean isOwner(Long reviewId, Long userId) {
        if (reviewId == null || userId == null) {
            return false;
        }
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || review.getUser() == null) {
            return false;
        }
        return review.getUser().getId().equals(userId);
    }
}
