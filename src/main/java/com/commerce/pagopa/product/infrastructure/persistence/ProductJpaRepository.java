package com.commerce.pagopa.product.infrastructure.persistence;

import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.model.enums.ProductStatus;
import com.commerce.pagopa.product.domain.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ProductJpaRepository extends JpaRepository<Product, Long>, ProductRepository, ProductRepositoryCustom {

    @Override
    Page<Product> findAllBySellerId(Long userId, Pageable pageable);

    @Override
    Page<Product> findAllByCategoryIdAndStatusIn(
            Long categoryId,
            Collection<ProductStatus> statuses,
            Pageable pageable
    );
}
