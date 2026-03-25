package com.commerce.pagopa.domain.product.repository;

import com.commerce.pagopa.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
