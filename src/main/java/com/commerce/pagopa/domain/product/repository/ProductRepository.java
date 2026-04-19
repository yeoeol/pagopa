package com.commerce.pagopa.domain.product.repository;

import com.commerce.pagopa.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Modifying
    @Query(value =
            "UPDATE Product p " +
            "SET p.stock = p.stock + :quantity " +
            "WHERE p.id = :productId " +
                    "AND :quantity > 0")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Modifying
    @Query(value =
            "UPDATE Product p " +
            "SET p.stock = p.stock - :quantity " +
            "WHERE p.id = :productId " +
                    "AND :quantity > 0 " +
                    "AND p.stock >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);
    
    @Query("SELECT p FROM Product p " +
           "WHERE (:name IS NULL " +
                    "OR :name = '' " +
                    "OR p.name LIKE CONCAT('%', :name, '%'))")
    List<Product> searchProducts(@Param("name") String name);

    Page<Product> findAllBySellerId(Long userId, Pageable pageable);
}