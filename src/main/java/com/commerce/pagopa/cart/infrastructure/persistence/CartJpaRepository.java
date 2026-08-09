package com.commerce.pagopa.cart.infrastructure.persistence;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.cart.domain.repository.CartRepository;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.user.domain.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<Cart, Long>, CartRepository {

    @Override
    @Query("""
            SELECT c
            FROM Cart c
                JOIN FETCH c.user u
                LEFT JOIN FETCH c.cartItems ci
                LEFT JOIN FETCH ci.product
            WHERE u.id = :userId
            """)
    Optional<Cart> findByUserIdWithItems(Long userId);

    // 단일 건 조회 시에도 FETCH JOIN 적용
    @Override
    @Query("SELECT DISTINCT c FROM Cart c " +
           "JOIN FETCH c.user u " +
           "JOIN FETCH c.product p " +
           "LEFT JOIN FETCH p.images " +
           "WHERE c.id = :id")
    Optional<Cart> findByIdWithFetch(@Param("id") Long id);

    @Override
    Optional<Cart> findByUserAndProduct(User user, Product product);

    @Override
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value =
            "DELETE FROM Cart c " +
            "WHERE c.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Override
    @Query(value =
            "SELECT c " +
            "FROM Cart c " +
            "JOIN FETCH c.product p " +
            "WHERE c.id IN :cartIds " +
                    "AND c.user.id = :userId")
    List<Cart> findAllByIdInAndUserId(@Param("cartIds") List<Long> cartIds, @Param("userId") Long userId);
}
