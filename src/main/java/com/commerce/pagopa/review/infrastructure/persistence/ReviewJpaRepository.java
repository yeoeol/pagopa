package com.commerce.pagopa.review.infrastructure.persistence;

import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.review.domain.model.Review;
import com.commerce.pagopa.review.domain.repository.ReviewRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<Review, Long>, ReviewRepository {

    @Override
    @Query("""
            SELECT DISTINCT r
            FROM Review r
                JOIN FETCH r.orderProduct op
                LEFT JOIN FETCH r.images ri
            WHERE op.product = :product
            """)
    List<Review> findAllByProduct(Product product);
}
