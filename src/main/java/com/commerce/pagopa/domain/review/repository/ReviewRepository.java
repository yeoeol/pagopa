package com.commerce.pagopa.domain.review.repository;

import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByOrderProduct_Product(Product orderProductProduct);
}
