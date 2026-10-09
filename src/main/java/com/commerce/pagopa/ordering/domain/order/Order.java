package com.commerce.pagopa.ordering.domain.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "orders",
        indexes = {@Index(
                name = "idx_orders_status_ordered_at",
                columnList = "status, ordered_at"
        )}
)
public class Order extends BaseTimeEntity {

    @Id
    @ToString.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "order_id",
            nullable = false
    )
    private Long id;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            length = 20,
            nullable = false
    )
    private OrderStatus status;

    @ToString.Include
    @Column(
            name = "ordered_at",
            nullable = false
    )
    private LocalDateTime orderedAt;

    @ToString.Include
    @Column(
            name = "canceled_at",
            nullable = true
    )
    private LocalDateTime canceledAt;

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.PERSIST
    )
    private final List<OrderItem> orderItems = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Order(OrderStatus status, LocalDateTime orderedAt, Long userId) {
        this.status = status;
        this.orderedAt = orderedAt;
        this.userId = userId;
    }

    public static Order init(Long userId) {
        return Order.builder()
                .status(OrderStatus.PENDING_PAYMENT)
                .orderedAt(LocalDateTime.now())
                .userId(userId)
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

    public boolean isOwner(Long userId) {
        return this.userId.equals(userId);
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
