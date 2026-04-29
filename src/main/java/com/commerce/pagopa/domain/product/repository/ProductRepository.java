package com.commerce.pagopa.domain.product.repository;

import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.entity.enums.ProductStatus;
import com.commerce.pagopa.global.exception.ProductNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    Page<Product> findAllBySellerId(Long userId, Pageable pageable);

    Page<Product> findAllByCategoryIdAndStatusIn(
            Long categoryId,
            Collection<ProductStatus> statuses,
            Pageable pageable
    );

    default Product getById(Long id) {
        return findById(id).orElseThrow(ProductNotFoundException::new);
    }
}
