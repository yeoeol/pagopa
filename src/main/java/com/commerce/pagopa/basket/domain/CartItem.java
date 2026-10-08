package com.commerce.pagopa.basket.domain;

import jakarta.persistence.*;

import lombok.*;

import com.commerce.pagopa.global.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "cart_item", uniqueConstraints = {
        @UniqueConstraint(name = "uq_cart_item_cart_id_product_id", columnNames = {"cart_id", "product_id"})})
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "cart_item_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_item_cart"))
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ToString.Include
    @Column(name = "cart_quantity", nullable = false)
    private Integer cartQuantity = 1;

    @Builder(access = AccessLevel.PRIVATE)
    private CartItem(Cart cart, Long productId, Integer cartQuantity) {
        this.cart = cart;
        this.productId = productId;
        this.cartQuantity = cartQuantity;
    }

    public static CartItem create(Cart cart, Long productId, Integer cartQuantity) {
        return CartItem.builder()
                .cart(cart)
                .productId(productId)
                .cartQuantity(cartQuantity)
                .build();
    }

    public void assignCart(Cart cart) {
        this.cart = cart;
    }

    public void addQuantity(Integer quantity) {
        this.cartQuantity += quantity;
    }

    public void reduceQuantity(Integer quantity) {
        if (this.cartQuantity >= quantity) {
            this.cartQuantity -= quantity;
        }
    }
}
