package com.commerce.pagopa.review.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.commerce.pagopa.review.domain.Review;
import com.commerce.pagopa.review.domain.ReviewRepository;

public interface ReviewJpaRepository extends JpaRepository<Review, Long>, ReviewRepository {

    @Override
    @Query("""
            SELECT DISTINCT r
            FROM Review r
                LEFT JOIN FETCH r.images ri
            WHERE r.productId = :productId
            """)
    List<Review> findAllWithDetailsByProductId(@Param("productId") Long productId);
}
