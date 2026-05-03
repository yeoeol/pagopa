package com.commerce.pagopa.order.domain.model;

import com.commerce.pagopa.product.domain.model.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_product")
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_id")
    private Long id;

    private int quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_order_id")
    private SellerOrder sellerOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderProduct(int quantity, BigDecimal price, SellerOrder sellerOrder, Product product) {
        this.quantity = quantity;
        this.price = price;
        this.sellerOrder = sellerOrder;
        this.product = product;
    }

    public static OrderProduct create(int quantity, BigDecimal price, Product product) {
        return OrderProduct.builder()
                .quantity(quantity)
                .price(price)
                .product(product)
                .build();
    }

    void assignSellerOrder(SellerOrder sellerOrder) {
        this.sellerOrder = sellerOrder;
    }

    public BigDecimal getTotalPrice() {
        return getPrice().multiply(BigDecimal.valueOf(getQuantity()));
    }
}
