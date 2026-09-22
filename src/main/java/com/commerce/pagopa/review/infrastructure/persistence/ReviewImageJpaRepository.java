package com.commerce.pagopa.review.infrastructure.persistence;

import com.commerce.pagopa.review.domain.ReviewImage;
import com.commerce.pagopa.review.domain.ReviewImageRepository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageJpaRepository extends JpaRepository<ReviewImage, Long>, ReviewImageRepository {
}
