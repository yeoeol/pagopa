package com.commerce.pagopa.cart.domain.model;

import com.commerce.pagopa.cartitem.domain.model.CartItem;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.user.domain.model.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_user")
    )
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private final List<CartItem> cartItems = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Cart(User user) {
        this.user = user;
    }

    public static Cart create(User user) {
        return Cart.builder()
                .user(user)
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
