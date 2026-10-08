package com.commerce.pagopa.review.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerce.pagopa.review.domain.ReviewImage;
import com.commerce.pagopa.review.domain.ReviewImageRepository;

public interface ReviewImageJpaRepository extends JpaRepository<ReviewImage, Long>, ReviewImageRepository {
}
