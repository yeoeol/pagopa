package com.commerce.pagopa.review.domain.repository;

import com.commerce.pagopa.global.exception.ReviewNotFoundException;
import com.commerce.pagopa.review.domain.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(Long id);

    List<Review> findAll();

    void deleteById(Long id);

    List<Review> findAllByProductId(Long productId);

    default Review findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(ReviewNotFoundException::new);
    }
}
