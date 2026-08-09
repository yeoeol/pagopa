package com.commerce.pagopa.cartitem.infrastructure.persistence;

import com.commerce.pagopa.cartitem.domain.model.CartItem;
import com.commerce.pagopa.cartitem.domain.repository.CartItemRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemJpaRepository extends JpaRepository<CartItem, Long>, CartItemRepository {
	@Override
	@Query(value = """
			SELECT ci
			FROM CartItem ci
				JOIN FETCH ci.product p
			WHERE ci.id IN :cartItemIds
				AND ci.cart.user.id = :userId
			""")
	List<CartItem> findAllByIdInAndUserId(
			@Param("cartItemIds") List<Long> cartItemIds,
			@Param("userId") Long userId
	);
}
