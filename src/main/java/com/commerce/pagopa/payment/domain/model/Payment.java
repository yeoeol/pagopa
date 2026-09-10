package com.commerce.pagopa.payment.domain.model;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.payment.domain.model.enums.PaymentStatus;
import jakarta.persistence.*;

import java.time.Instant;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_payment_order_id",
                        columnNames = "order_id"
                ),
                @UniqueConstraint(
                        name = "uq_payment_provider_transaction_id",
                        columnNames = "provider_transaction_id"
                )
        }
)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "payment_id", nullable = false)
    private Long id;

    @ToString.Include
    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod;

    @ToString.Include
    @Column(name = "amount", nullable = false)
    private Integer amount; // 결제 금액

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PaymentStatus status; // 결제 상태

    @ToString.Include
    @Column(name = "paid_at", nullable = true)
    private Instant paidAt;

    @ToString.Include
    @Column(name = "canceled_at", nullable = true)
    private Instant canceledAt;

    @ToString.Include
    @Column(name = "provider_transaction_id", length = 255, nullable = true)
    private String providerTransactionId;

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

    public void approve(
            String providerTransactionId,
            Integer approveAmount,
            Instant approvedAt
    ) {
        if (this.status != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.PAYMENT_REQUEST_ERROR);
        }
        if (!this.amount.equals(approveAmount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        this.providerTransactionId = providerTransactionId;
        this.paidAt = approvedAt;
        this.status = PaymentStatus.PAID;
    }

    // 이미 결제완료 상태인 엔티티는 환불 요청으로 처리해야 함
    public void cancel(
            String providerTransactionId,
            Integer canceledAmount,
            Instant canceledAt
    ) {
        if (!this.providerTransactionId.equals(providerTransactionId)) {
            throw new BusinessException(ErrorCode.PAYMENT_REQUEST_ERROR);
        }
        if (this.status == PaymentStatus.CANCELED) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
        }
        if (this.status != PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_CANCELABLE);
        }
        if (!this.amount.equals(canceledAmount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        this.canceledAt = canceledAt;
        this.status = PaymentStatus.CANCELED;
    }
}
