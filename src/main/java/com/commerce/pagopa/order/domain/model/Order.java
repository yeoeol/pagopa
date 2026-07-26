package com.commerce.pagopa.order.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.orderitem.domain.model.OrderItem;
import com.commerce.pagopa.user.domain.model.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "orders",
        indexes = {
                @Index(
                        name = "idx_orders_status_ordered_at",
                        columnList = "status, ordered_at"
                )
        }
)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Long id;

    @Column(unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private String orderName;

    @Column(nullable = false)
    private Integer totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt;

    private Instant cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_orders_user")
    )
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> orderItems = new ArrayList<>();


    @Builder(access = AccessLevel.PRIVATE)
    private Order(
            String orderNumber,
            User user,
            Instant orderedAt,
            Instant cancelledAt
    ) {
        this.orderNumber = orderNumber;
        this.user = user;
        this.totalAmount = 0;
        this.orderName = "";
        this.status = OrderStatus.ORDERED;
        this.orderedAt = orderedAt;
        this.cancelledAt = cancelledAt;
    }

    public static Order init(User user) {
        return Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .orderedAt(Instant.now())
                .cancelledAt(null)
                .build();
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.assignOrder(this);
        addTotalPrice(orderItem.getTotalPrice());
    }

    private void addTotalPrice(Integer totalPrice) {
        this.totalAmount += totalPrice;
    }

    private static String generateOrderNumber() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // == 주문 취소 로직 ==
    public void cancel() {
        validateCancelOrder();
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    private void validateCancelOrder() {
        if (this.status == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }
        if (this.status != OrderStatus.ORDERED) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }
    }
}
