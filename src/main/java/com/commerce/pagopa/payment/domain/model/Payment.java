package com.commerce.pagopa.payment.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.payment.domain.model.enums.PaymentStatus;
import jakarta.persistence.*;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_payment_order_id",
                        columnNames = "order_id"
                )
        }
)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long id;

    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod;

    @Column(name = "amount", nullable = false)
    private Integer amount; // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PaymentStatus status; // 결제 상태

    @Column(name = "paid_at", nullable = true)
    private Instant paidAt;

    @Column(name = "canceled_at", nullable = true)
    private Instant canceledAt;

    @Column(unique = true, length = 200)
    private String paymentKey; // 토스 페이먼츠에서 발급하는 고유 키

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_orders")
    )
    private Order order;

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(
            String paymentMethod,
            Integer amount,
            PaymentStatus status,
            Order order
    ) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
        this.order = order;
    }

    public static Payment create(String paymentMethod, Integer amount, Order order) {
        return Payment.builder()
                .paymentMethod(paymentMethod)
                .amount(amount)
                .status(PaymentStatus.READY)
                .order(order)
                .build();
    }

    public void pay(String paymentKey) {
        this.paidAt = Instant.now();
        this.status = PaymentStatus.PAID;
        this.paymentKey = paymentKey;
    }
}
