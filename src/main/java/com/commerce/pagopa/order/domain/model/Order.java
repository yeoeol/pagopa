package com.commerce.pagopa.order.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.orderitem.domain.model.OrderItem;
import com.commerce.pagopa.user.domain.model.User;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
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
    @ToString.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Long id;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private OrderStatus status;

    @ToString.Include
    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @ToString.Include
    @Column(name = "canceled_at", nullable = true)
    private LocalDateTime canceledAt;

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
            LocalDateTime orderedAt,
            User user
    ) {
        this.status = status;
        this.orderedAt = orderedAt;
        this.user = user;
    }

    public static Order init(User user) {
        return Order.builder()
                .status(OrderStatus.PENDING_PAYMENT)
                .orderedAt(LocalDateTime.now())
                .user(user)
                .build();
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.assignOrder(this);
    }

    public void confirmPayment(int paidAmount) {
        validateConfirmPayment();
        if (getTotalAmount() != paidAmount) {
            throw new BusinessException(ErrorCode.ORDER_INCORRECT_AMOUNT);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel(LocalDateTime canceledAt) {
        validateCancelable();
        validateCanceledAt(canceledAt);

        this.status = OrderStatus.CANCELED;
        this.canceledAt = canceledAt;
    }

    public void cancelAfterPayment(LocalDateTime canceledAt) {
        validateCancelAfterPayment();
        validateCanceledAt(canceledAt);

        this.status = OrderStatus.CANCELED;
        this.canceledAt = canceledAt;
    }

    public Integer getTotalAmount() {
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }

    // == 상태 검증 메서드 == //
    public void validateConfirmPayment() {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_PAY);
        }
    }

    public void validateCancelable() {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }
    }

    public void validateCancelAfterPayment() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }
    }

    private void validateCanceledAt(LocalDateTime canceledAt) {
        if (canceledAt == null || canceledAt.isBefore(this.orderedAt)) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }
    }
}
