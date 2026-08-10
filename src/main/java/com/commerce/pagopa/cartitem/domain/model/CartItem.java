package com.commerce.pagopa.cartitem.domain.model;

import com.commerce.pagopa.cart.domain.model.Cart;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.product.domain.model.Product;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
		name = "cart_item",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uq_cart_item_cart_id_product_id",
						columnNames = {"cart_id", "product_id"}
				)
		}
)
public class CartItem extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cart_item_id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "cart_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_cart_item_cart")
	)
	private Cart cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "product_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_cart_item_product")
	)
	private Product product;

	@Column(name = "cart_quantity", nullable = false)
	private Integer cartQuantity = 1;

	@Builder(access = AccessLevel.PRIVATE)
	private CartItem(
			Cart cart,
			Product product,
			Integer cartQuantity
	) {
		this.cart = cart;
		this.product = product;
		this.cartQuantity = cartQuantity;
	}

	public static CartItem create(
			Cart cart,
			Product product,
			Integer cartQuantity
	) {
		return CartItem.builder()
				.cart(cart)
				.product(product)
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
