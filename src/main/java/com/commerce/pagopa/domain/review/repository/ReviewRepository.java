package com.commerce.pagopa.domain.review.repository;

import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("""
            SELECT DISTINCT r
            FROM Review r
                JOIN FETCH r.orderProduct op
                JOIN FETCH r.images ri
            WHERE op.product = :product
            """)
    List<Review> findAllByOrderProduct_Product(Product product);
}
