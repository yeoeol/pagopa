package com.commerce.pagopa.domain.cart.repository;

import com.commerce.pagopa.domain.cart.entity.Cart;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUser(User user);
    Optional<Cart> findByUserAndProduct(User user, Product product);

    @Modifying
    @Query(value =
            "DELETE FROM Cart c " +
            "WHERE c.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query(value =
            "SELECT c " +
            "FROM Cart c " +
            "WHERE c.id IN :cartIds " +
                    "AND c.user.id = :userId")
    List<Cart> findAllByIdInAndUserId(@Param("cartIds") List<Long> cartIds, @Param("userId") Long userId);
}
