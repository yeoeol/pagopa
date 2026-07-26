package com.commerce.pagopa.payment.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
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

    @Column(nullable = false)
    private Integer cancelledAmount; // 누적 환불 금액 (부분 취소 합산)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status; // 결제 상태

    @Column(name = "paid_at", nullable = true)
    private Instant paidAt;

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
            Instant paidAt,
            String paymentKey,
            Order order
    ) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
        this.paidAt = paidAt;
        this.paymentKey = paymentKey;
        this.order = order;
    }

    public static Payment create(String paymentMethod, Order order) {
        return Payment.builder()
                .paymentMethod(paymentMethod)
                .amount(order.getTotalAmount())
                .order(order)
                .build();
    }

    // 결제 진행 중 상태로 변경 (재시도 시를 위해 FAILED, CANCELLED는 재사용 허용)
    public void markInProgress() {
        if (this.status == PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_COMPLETED, "이미 처리 완료된 결제 건입니다. (현재 상태: " + this.status + ")");
        }
        this.status = PaymentStatus.IN_PROGRESS;
    }

    public void validateConfirmable() {
        if (this.status == PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
        }
        if (this.status != PaymentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_IN_PROGRESS);
        }
    }

    // 결제 승인 완료
    public void success(String paymentKey) {
        validateConfirmable();
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.PAID;
    }
}
