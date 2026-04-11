package com.commerce.pagopa.domain.review.repository;

import com.commerce.pagopa.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
