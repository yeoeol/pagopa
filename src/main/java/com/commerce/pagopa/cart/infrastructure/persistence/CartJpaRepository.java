package com.commerce.pagopa.cart.infrastructure.persistence;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.cart.domain.repository.CartRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<Cart, Long>, CartRepository {

    @Override
    @Query(value = """
            SELECT c
            FROM Cart c
                JOIN FETCH c.user u
                LEFT JOIN FETCH c.cartItems ci
                LEFT JOIN FETCH ci.product p
                LEFT JOIN FETCH p.category
                LEFT JOIN FETCH p.seller
            WHERE u.id = :userId
            """)
    Optional<Cart> findByUserIdWithItems(Long userId);
}
