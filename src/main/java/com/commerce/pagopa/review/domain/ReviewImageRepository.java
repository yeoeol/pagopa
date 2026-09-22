package com.commerce.pagopa.review.domain;

import java.util.Optional;

public interface ReviewImageRepository {

    ReviewImage save(ReviewImage reviewImage);

    Optional<ReviewImage> findById(Long id);

    void deleteById(Long id);
}
