package com.commerce.pagopa.product.infrastructure.persistence;

import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.model.enums.ProductStatus;
import com.commerce.pagopa.product.domain.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long>, ProductRepository, ProductRepositoryCustom {

    @Override
    Page<Product> findAllBySellerId(Long userId, Pageable pageable);

    @Override
    @Modifying
    @Query("UPDATE Product p " +
            "SET p.stock = p.stock - :quantity " +
            "WHERE p.id = :productId AND p.stock >= :quantity AND :quantity > 0")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Override
    @Modifying
    @Query("UPDATE Product p " +
            "SET p.stock = p.stock + :quantity " +
            "WHERE p.id = :productId AND :quantity > 0")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);

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
}
