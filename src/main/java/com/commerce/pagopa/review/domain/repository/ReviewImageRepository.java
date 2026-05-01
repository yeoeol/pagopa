package com.commerce.pagopa.review.domain.repository;

import com.commerce.pagopa.review.domain.model.ReviewImage;

import java.util.Optional;

public interface ReviewImageRepository {

    ReviewImage save(ReviewImage reviewImage);

    Optional<ReviewImage> findById(Long id);

    void deleteById(Long id);
}
