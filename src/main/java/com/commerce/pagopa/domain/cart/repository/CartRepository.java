package com.commerce.pagopa.domain.cart.repository;

import com.commerce.pagopa.domain.cart.entity.Cart;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUser(User user);
    Optional<Cart> findByUserAndProduct(User user, Product product);
}
