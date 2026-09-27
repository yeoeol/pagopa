package com.commerce.pagopa.basket.domain;

import com.commerce.pagopa.global.entity.BaseTimeEntity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "cart",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_cart_user_id",
                        columnNames = {"user_id"}
                )
        }
)
public class Cart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(
            mappedBy = "cart",
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.REMOVE
            },
            orphanRemoval = true
    )
    private final List<CartItem> cartItems = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Cart(Long userId) {
        this.userId = userId;
    }

    public static Cart create(Long userId) {
        return Cart.builder()
                .userId(userId)
                .build();
    }

    public void addItem(CartItem cartItem) {
        this.cartItems.add(cartItem);
        cartItem.assignCart(this);
    }

    public void removeAllItems() {
        this.cartItems.clear();
    }
}
