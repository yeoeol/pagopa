package com.commerce.pagopa.review.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.commerce.pagopa.review.domain.Review;
import com.commerce.pagopa.review.domain.ReviewRepository;

public interface ReviewJpaRepository extends JpaRepository<Review, Long>, ReviewRepository {

    @Override
    @Query(value = """
            SELECT r
            FROM Review r
                LEFT JOIN FETCH r.images ri
            WHERE r.productId = :productId
            ORDER BY r.createdAt desc, r.id desc
            """,
            countQuery = """
                    SELECT count(r)
                    FROM Review r
                    WHERE r.productId = :productId
                    """
    )
    Page<Review> findAllWithDetailsByProductId(
            @Param("productId") Long productId,
            Pageable pageable
    );
}
