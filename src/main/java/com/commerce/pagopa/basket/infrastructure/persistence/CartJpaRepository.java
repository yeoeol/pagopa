package com.commerce.pagopa.basket.infrastructure.persistence;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.commerce.pagopa.basket.domain.Cart;
import com.commerce.pagopa.basket.domain.CartRepository;

public interface CartJpaRepository extends JpaRepository<Cart, Long>, CartRepository {

    @Override
    @Query(value = """
            SELECT c
            FROM Cart c
                LEFT JOIN FETCH c.cartItems ci
            WHERE c.userId = :userId
            """)
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
            SELECT c
            FROM Cart c
                LEFT JOIN FETCH c.cartItems ci
            WHERE c.userId = :userId
            """)
    Optional<Cart> findByUserIdWithItemsForUpdate(@Param("userId") Long userId);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
            SELECT c
            FROM Cart c
                LEFT JOIN FETCH c.cartItems ci
            WHERE c.userId = :userId
            """)
    Optional<Cart> findByUserIdForUpdate(@Param("userId") Long userId);
}
