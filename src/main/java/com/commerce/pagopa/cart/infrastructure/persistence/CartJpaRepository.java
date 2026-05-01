package com.commerce.pagopa.cart.infrastructure.persistence;

import com.commerce.pagopa.cart.domain.repository.CartRepository;
import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.domain.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface CartJpaRepository extends JpaRepository<Cart, Long>, CartRepository {

    // FETCH JOIN 적용하여 연관 엔티티(user, product, product.images)를 한 번에 조회
    @Override
    @Query("SELECT DISTINCT c FROM Cart c " +
           "JOIN FETCH c.user u " +
           "JOIN FETCH c.product p " +
           "LEFT JOIN FETCH p.images " +
           "WHERE c.user = :user")
    List<Cart> findByUser(@Param("user") User user);

    // 단일 건 조회 시에도 FETCH JOIN 적용
    @Override
    @Query("SELECT DISTINCT c FROM Cart c " +
           "JOIN FETCH c.user u " +
           "JOIN FETCH c.product p " +
           "LEFT JOIN FETCH p.images " +
           "WHERE c.id = :id")
    Optional<Cart> findByIdWithFetch(@Param("id") Long id);

    @Override
    Optional<Cart> findByUserAndProduct(User user, Product product);

    @Override
    @Modifying
    @Query(value =
            "DELETE FROM Cart c " +
            "WHERE c.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Override
    @Query(value =
            "SELECT c " +
            "FROM Cart c " +
            "JOIN FETCH c.product p " +
            "WHERE c.id IN :cartIds " +
                    "AND c.user.id = :userId")
    List<Cart> findAllByIdInAndUserId(@Param("cartIds") List<Long> cartIds, @Param("userId") Long userId);
}
