package com.commerce.pagopa.cart.domain.repository;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.exception.CartNotFoundException;

import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartRepository {
    Cart save(Cart cart);

    Optional<Cart> findById(Long id);

    void deleteById(Long id);

    void deleteAllByIdIn(Collection<Long> ids);

    // FETCH JOIN 적용하여 연관 엔티티(user, product, product.images)를 한 번에 조회
    List<Cart> findByUser(User user);

    // 단일 건 조회 시에도 FETCH JOIN 적용
    Optional<Cart> findByIdWithFetch(Long id);

    Optional<Cart> findByUserAndProduct(User user, Product product);

    void deleteAllByUserId(@Param("userId") Long userId);

    List<Cart> findAllByIdInAndUserId(@Param("cartIds") List<Long> cartIds, @Param("userId") Long userId);

    default Cart findByIdWithFetchOrThrow(Long id) {
        return findByIdWithFetch(id).orElseThrow(CartNotFoundException::new);
    }

}
