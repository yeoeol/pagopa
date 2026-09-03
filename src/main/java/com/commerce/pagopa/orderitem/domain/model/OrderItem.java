package com.commerce.pagopa.orderitem.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.product.domain.model.Product;
import jakarta.persistence.*;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "order_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_order_item_order_id_product_id",
                        columnNames = {"order_id", "product_id"}
                )
        }
)
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "order_item_id", nullable = false)
    private Long id;

    @ToString.Include
    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    @ToString.Include
    @Column(name = "order_price", nullable = false)
    private Integer orderPrice;

    @ToString.Include
    @Column(name = "order_quantity", nullable = false)
    private Integer orderQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_orders")
    )
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_product")
    )
    private Product product;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItem(
            String productName,
            Integer orderPrice,
            Integer orderQuantity,
            Order order,
            Product product
    ) {
        this.productName = productName;
        this.orderPrice = orderPrice;
        this.orderQuantity = orderQuantity;
        this.order = order;
        this.product = product;
    }

    public static OrderItem create(
            String productName,
            Integer orderPrice,
            Integer orderQuantity,
            Order order,
            Product product
    ) {
        return OrderItem.builder()
                .productName(productName)
                .orderPrice(orderPrice)
                .orderQuantity(orderQuantity)
                .order(order)
                .product(product)
                .build();
    }

    public Integer getTotalPrice() {
        return orderPrice * orderQuantity;
    }

    public void assignOrder(Order order) {
        this.order = order;
    }
}
