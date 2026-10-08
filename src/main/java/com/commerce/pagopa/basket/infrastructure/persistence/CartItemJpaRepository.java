package com.commerce.pagopa.basket.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.commerce.pagopa.basket.domain.CartItem;
import com.commerce.pagopa.basket.domain.CartItemRepository;

public interface CartItemJpaRepository extends JpaRepository<CartItem, Long>, CartItemRepository {
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
            SELECT ci
            FROM CartItem ci
            WHERE ci.id IN :cartItemIds
                AND ci.cart.userId = :userId
            ORDER BY ci.id
            """)
    List<CartItem> findAllByIdInAndUserIdForUpdate(
            @Param("cartItemIds") List<Long> cartItemIds,
            @Param("userId") Long userId
    );

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
            SELECT ci
            FROM CartItem ci
            WHERE ci.id = :cartItemId
            """)
    Optional<CartItem> findByIdForUpdate(@Param("cartItemId") Long cartItemId);
}
