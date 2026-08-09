package com.commerce.pagopa.cartitem.domain.repository;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.cartitem.domain.model.CartItem;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.product.domain.model.Product;

import java.util.Optional;

public interface CartItemRepository {
	CartItem save(CartItem cartItem);

	Optional<CartItem> findById(Long cartItemId);

	Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

	void deleteById(Long cartItemId);

	default CartItem findByIdOrThrow(Long cartItemId) {
		return findById(cartItemId).orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
	}
}
