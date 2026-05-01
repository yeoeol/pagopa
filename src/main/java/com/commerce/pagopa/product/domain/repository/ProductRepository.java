package com.commerce.pagopa.product.domain.repository;

import com.commerce.pagopa.global.exception.ProductNotFoundException;
import com.commerce.pagopa.product.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.model.enums.ProductStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    List<Product> findAll();

    Page<Product> findAll(Pageable pageable);

    boolean existsById(Long id);

    void deleteById(Long id);

    Page<Product> findAllBySellerId(Long userId, Pageable pageable);

    Page<Product> findAllByCategoryIdAndStatusIn(
            Long categoryId,
            Collection<ProductStatus> statuses,
            Pageable pageable
    );

    List<Product> searchProducts(ProductSearchCondition condition);

    default Product findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(ProductNotFoundException::new);
    }
}
