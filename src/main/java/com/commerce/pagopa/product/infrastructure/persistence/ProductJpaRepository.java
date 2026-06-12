package com.commerce.pagopa.product.infrastructure.persistence;

import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long>, ProductRepository, ProductRepositoryCustom {

    @Override
    Page<Product> findAllBySellerId(Long userId, Pageable pageable);

    @Override
    List<Product> findAllByIdIn(List<Long> productIds);

    @Override
    @Query("""
            SELECT p
            FROM Product p
                JOIN FETCH p.category c
                LEFT JOIN FETCH c.parent pc
                LEFT JOIN FETCH pc.parent gpc
                JOIN FETCH p.seller s
                LEFT JOIN FETCH p.images pi
            WHERE p.id = :productId
            """)
    Optional<Product> findByIdWithCategoryParentsAndSellerAndProductImages(@Param("productId") Long productId);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
