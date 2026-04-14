package com.commerce.pagopa.domain.payment.entity;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.payment.entity.enums.PaymentStatus;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status; // 결제 상태

    @Column(unique = true, length = 200)
    private String paymentKey; // 토스 페이먼츠에서 발급하는 고유 키

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(Order order, BigDecimal amount, PaymentStatus status) {
        this.order = order;
        this.amount = amount;
        this.status = status;
    }

    public static Payment create(Order order) {
        return Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.READY) // 초기 상태: 결제 대기
                .build();
    }

    // 결제 진행 중 상태로 변경
    public void setInProgress() {
        this.status = PaymentStatus.IN_PROGRESS;
    }

    // 결제 승인 완료
    public void success(String paymentKey) {
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.PAID;
    }

    // 결제 실패
    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    // 결제 취소
    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }
}