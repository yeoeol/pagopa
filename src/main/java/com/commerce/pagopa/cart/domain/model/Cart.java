package com.commerce.pagopa.cart.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.user.domain.model.User;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.commerce.pagopa.global.response.ErrorCode.INVALID_CART_QUANTITY;

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

    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw new BusinessException(INVALID_CART_QUANTITY);
        }
        this.quantity += amount;
    }

    public void reduceQuantity() {
        if (this.quantity <= 0) {
            throw new BusinessException(INVALID_CART_QUANTITY);
        }
        this.quantity -= 1;
    }
}
