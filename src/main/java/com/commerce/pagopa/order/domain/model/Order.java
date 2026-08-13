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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private OrderStatus status;

    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt;

    @Column(name = "canceled_at", nullable = true)
    private Instant canceledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_orders_user")
    )
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private final List<OrderItem> orderItems = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Order(
            OrderStatus status,
            Instant orderedAt,
            User user
    ) {
        this.status = status;
        this.orderedAt = orderedAt;
        this.user = user;
    }

    public static Order init(User user) {
        return Order.builder()
                .status(OrderStatus.PENDING_PAYMENT)
                .orderedAt(Instant.now())
                .user(user)
                .build();
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.assignOrder(this);
    }

    // == 주문 취소 로직 ==
    public void cancel(Instant canceledAt) {
        if (canceledAt == null || canceledAt.isBefore(this.orderedAt)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        this.status = OrderStatus.CANCELED;
        this.canceledAt = canceledAt;
    }
}
