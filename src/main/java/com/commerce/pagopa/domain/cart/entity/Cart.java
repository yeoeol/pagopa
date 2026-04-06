package com.commerce.pagopa.domain.cart.entity;

import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "carts")
public class Cart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder(access = AccessLevel.PRIVATE)
    private Cart(int quantity, User user, Product product) {
        this.quantity = quantity;
        this.user = user;
        this.product = product;
    }

    public static Cart create(int quantity, User user, Product product) {
        return Cart.builder()
                .quantity(quantity)
                .user(user)
                .product(product)
                .build();
    }

    // 수량 증가 메서드
    public void addQuantity() {
        this.quantity += 1;
    }

    // 수량 감소 메서드
    public void reduceQuantity() {
        this.quantity -= 1;
    }
}
