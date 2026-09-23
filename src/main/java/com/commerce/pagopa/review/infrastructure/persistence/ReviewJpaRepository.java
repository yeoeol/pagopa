package com.commerce.pagopa.review.infrastructure.persistence;

import com.commerce.pagopa.review.domain.Review;
import com.commerce.pagopa.review.domain.ReviewRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<Review, Long>, ReviewRepository {

    @Override
    @Query("""
            SELECT DISTINCT r
            FROM Review r
                LEFT JOIN FETCH r.images ri
            WHERE p.id = :productId
            """)
    List<Review> findAllWithDetailsByProductId(@Param("productId") Long productId);
}
