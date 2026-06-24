package com.commerce.pagopa.review.domain.repository;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.review.domain.model.Review;

import java.util.List;
import java.util.Optional;

import static com.commerce.pagopa.global.response.ErrorCode.REVIEW_NOT_FOUND;

public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(Long id);

    List<Review> findAll();

    void deleteById(Long id);

    List<Review> findAllByProductIdWithUserAndReviewImages(Long productId);

    default Review findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new BusinessException(REVIEW_NOT_FOUND));
    }
}
