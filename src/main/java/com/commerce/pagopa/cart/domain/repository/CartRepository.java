package com.commerce.pagopa.cart.domain.repository;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.global.exception.BusinessException;

import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.commerce.pagopa.global.response.ErrorCode.CART_NOT_FOUND;

public interface CartRepository {
    Cart save(Cart cart);

    Optional<Cart> findById(Long id);

    void deleteById(Long id);

    void deleteAllByIdIn(Collection<Long> ids);

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findByUserIdWithItems(Long userId);

    List<Cart> findAllByIdInAndUserId(@Param("cartIds") List<Long> cartIds, @Param("userId") Long userId);

    default Cart findByUserIdOrThrow(Long userId) {
        return findByUserId(userId).orElseThrow(() -> new BusinessException(CART_NOT_FOUND));
    }
}
