package com.commerce.pagopa.catalog.infrastructure.persistence;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.catalog.domain.ProductRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long>, ProductRepository, ProductRepositoryCustom {

    @Override
    Page<Product> findAllByUserId(Long userId, Pageable pageable);

    @Override
    @Query("""
            SELECT p
            FROM Product p
                JOIN FETCH p.category c
                LEFT JOIN FETCH c.parent pc
                LEFT JOIN FETCH pc.parent gpc
                LEFT JOIN FETCH p.images pi
            WHERE p.id = :productId
            """)
    Optional<Product> findByIdWithCategoryParentsAndSellerAndProductImages(@Param("productId") Long productId);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
