package com.commerce.pagopa.ordering.domain.order;

import jakarta.persistence.*;

import lombok.*;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "order_item", uniqueConstraints = {
        @UniqueConstraint(name = "uq_order_item_order_id_product_id", columnNames = {"order_id", "product_id"})})
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
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_item_orders"))
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItem(String productName, Integer orderPrice, Integer orderQuantity, Order order, Long productId) {
        this.productName = productName;
        this.orderPrice = orderPrice;
        this.orderQuantity = orderQuantity;
        this.order = order;
        this.productId = productId;
    }

    public static OrderItem create(
            String productName,
            Integer orderPrice,
            Integer orderQuantity,
            Order order,
            Long productId
    ) {
        return OrderItem.builder()
                .productName(productName)
                .orderPrice(orderPrice)
                .orderQuantity(orderQuantity)
                .order(order)
                .productId(productId)
                .build();
    }

    public Integer getTotalPrice() {
        return orderPrice * orderQuantity;
    }

    public void assignOrder(Order order) {
        this.order = order;
    }

    public void validateOrderUser(Long userId) {
        if (!order.isOwner(userId)) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_MINE);
        }
    }
}
