package com.commerce.pagopa.cartitem.infrastructure.persistence;

import com.commerce.pagopa.cartitem.domain.model.CartItem;
import com.commerce.pagopa.cartitem.domain.repository.CartItemRepository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemJpaRepository extends JpaRepository<CartItem, Long>, CartItemRepository {
}
