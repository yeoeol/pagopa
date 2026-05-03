package com.commerce.pagopa.order.domain.model;

import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.model.enums.PaymentMethod;
import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.OrderCannotPayException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private String orderName;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 한 Order의 모든 SellerOrder는 동일 배송지로 발송된다.
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<SellerOrder> sellerOrders = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Order(String orderNumber, PaymentMethod paymentMethod, User user, Delivery delivery) {
        this.orderNumber = orderNumber;
        this.paymentMethod = paymentMethod;
        this.user = user;
        this.delivery = delivery;
        this.totalAmount = BigDecimal.ZERO;
    }

    public static Order init(String orderNumber, PaymentMethod paymentMethod, User user, Delivery delivery) {
        return Order.builder()
                .orderNumber(orderNumber)
                .paymentMethod(paymentMethod)
                .user(user)
                .delivery(delivery)
                .build();
    }

    public void addSellerOrder(SellerOrder sellerOrder) {
        this.sellerOrders.add(sellerOrder);
        sellerOrder.assignOrder(this);
        recalcTotal();
    }

    public void recalcTotal() {
        this.totalAmount = sellerOrders.stream()
                .map(SellerOrder::getSellerTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void assignOrderName(String orderName) {
        this.orderName = orderName;
    }

    /**
     * - 비어있음: ORDERED (초기 생성, SellerOrder 미부착)
     * - 모두 PENDING_PAYMENT → ORDERED
     * - 모두 CANCELLED → CANCELLED
     * - 모두 COMPLETED → COMPLETED
     * - 모두 READY → PAID
     * - 그 외 (혼재) → DELIVERING (= 진행 중)
     */
    public OrderStatus getStatus() {
        if (sellerOrders.isEmpty()) {
            return OrderStatus.ORDERED;
        }
        if (allSellerOrdersAre(SellerOrderStatus.PENDING_PAYMENT)) {
            return OrderStatus.ORDERED;
        }
        if (allSellerOrdersAre(SellerOrderStatus.CANCELLED)) {
            return OrderStatus.CANCELLED;
        }
        if (allSellerOrdersAre(SellerOrderStatus.COMPLETED)) {
            return OrderStatus.COMPLETED;
        }
        if (allSellerOrdersAre(SellerOrderStatus.READY)) {
            return OrderStatus.PAID;
        }
        return OrderStatus.DELIVERING;
    }

    private boolean allSellerOrdersAre(SellerOrderStatus status) {
        return sellerOrders.stream().allMatch(so -> so.getStatus() == status);
    }

    public void validatePayable() {
        if (getStatus() != OrderStatus.ORDERED) {
            throw new OrderCannotPayException();
        }
    }

    /** 결제 승인 시: 모든 SellerOrder를 READY로 전환 */
    public void pay() {
        validatePayable();
        sellerOrders.forEach(SellerOrder::pay);
    }

    /** 전체 주문 취소: 취소 가능한 SellerOrder만 취소(이미 CANCELLED는 스킵). 발송 후 SellerOrder가 있으면 예외 발생 */
    public void cancel() {
        List<SellerOrder> activeSellerOrders = sellerOrders.stream()
                .filter(so -> !so.isCancelled())
                .toList();

        activeSellerOrders.forEach(SellerOrder::validateCancelable);    // 원자성을 위해 미리 검증
        activeSellerOrders.forEach(SellerOrder::cancel);
    }
}
