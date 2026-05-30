package com.commerce.pagopa.review.infrastructure.persistence;

import com.commerce.pagopa.review.domain.model.Review;
import com.commerce.pagopa.review.domain.repository.ReviewRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<Review, Long>, ReviewRepository {

    @Override
    @Query("""
            SELECT DISTINCT r
            FROM Review r
                JOIN r.orderProduct op
                JOIN FETCH r.user u
                LEFT JOIN FETCH r.images ri
            WHERE op.product.id = :productId
            """)
    List<Review> findAllByProductId(@Param("productId") Long productId);
}
