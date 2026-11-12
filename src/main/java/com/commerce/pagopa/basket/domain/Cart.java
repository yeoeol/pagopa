package com.commerce.pagopa.basket.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "cart",
        uniqueConstraints = {@UniqueConstraint(
                name = "uq_cart_user_id",
                columnNames = {"user_id"}
        )}
)
public class Cart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "cart_id",
            nullable = false
    )
    private Long id;

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @OneToMany(
            mappedBy = "cart",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
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
    }

    public void removeAllItems() {
        this.cartItems.clear();
    }

    public void validateOwner(Long userId) {
        if (!isOwner(userId)) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_MINE);
        }
    }

    public boolean isOwner(Long userId) {
        return this.userId.equals(userId);
    }

    public CartItem incrementItem(Long productId) {
        CartItem cartItem = getItemByProductId(productId);
        if (cartItem == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_MINE);
        }

        cartItem.addQuantity(1);
        return cartItem;
    }

    public CartItem decrementItem(Long productId) {
        CartItem cartItem = getItemByProductId(productId);
        if (cartItem == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_MINE);
        }

        cartItem.reduceQuantity(1);
        if (cartItem.getCartQuantity() <= 0) {
            cartItems.remove(cartItem);
        }
        return cartItem;
    }

    public CartItem getCartItem(Long cartItemId) {
        return cartItems.stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    public CartItem getItemByProductId(Long productId) {
        return cartItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    public void addItemQuantity(
            Long cartItemId,
            Integer quantity
    ) {
        getCartItem(cartItemId).addQuantity(quantity);
    }

    public List<CartItem> getItemsByIds(List<Long> cartItemIds) {
        List<CartItem> items = new ArrayList<>();
        cartItems.forEach(cartItem -> {
            if (!cartItemIds.contains(cartItem.getId())) {
                throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
            }
            items.add(cartItem);
        });
        return items;
    }

    public void deleteItem(Long productId) {
        cartItems.removeIf(item -> item.getProductId().equals(productId));
    }

    public void deleteAll(List<Long> cartItemIds) {
        cartItemIds.forEach(this::deleteItem);
    }
}
