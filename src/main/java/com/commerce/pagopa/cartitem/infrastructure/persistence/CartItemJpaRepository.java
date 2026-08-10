package com.commerce.pagopa.cartitem.infrastructure.persistence;

import com.commerce.pagopa.cartitem.domain.model.CartItem;
import com.commerce.pagopa.cartitem.domain.repository.CartItemRepository;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemJpaRepository extends JpaRepository<CartItem, Long>, CartItemRepository {
	@Override
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query(value = """
			SELECT ci
			FROM CartItem ci
			WHERE ci.id IN :cartItemIds
				AND ci.cart.user.id = :userId
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
