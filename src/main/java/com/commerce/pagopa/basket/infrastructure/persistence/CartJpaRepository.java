package com.commerce.pagopa.basket.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
    Optional<Cart> findByUserIdWithItems(Long userId);
}
