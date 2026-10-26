package com.commerce.pagopa.payment.domain;

import java.time.LocalDateTime;

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
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_payment_order_id",
                        columnNames = "order_id"
                ),
                @UniqueConstraint(
                        name = "uq_payment_provider_payment_id",
                        columnNames = {"provider", "provider_payment_id"}
                )}
)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(
            name = "payment_id",
            nullable = false
    )
    private Long id;

    @ToString.Include
    @Column(
            name = "payment_method",
            length = 50,
            nullable = false
    )
    private String paymentMethod;

    @ToString.Include
    @Column(
            name = "amount",
            nullable = false
    )
    private Integer amount; // 결제 금액

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            length = 20,
            nullable = false
    )
    private PaymentStatus status; // 결제 상태

    @ToString.Include
    @Column(
            name = "paid_at",
            nullable = true
    )
    private LocalDateTime paidAt;

    @ToString.Include
    @Column(
            name = "canceled_at",
            nullable = true
    )
    private LocalDateTime canceledAt;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(
            name = "provider",
            length = 20,
            nullable = false
    )
    private PaymentProvider provider;

    @ToString.Include
    @Column(
            name = "provider_payment_id",
            length = 255,
            nullable = true
    )
    private String providerPaymentId;

    @Column(
            name = "order_id",
            nullable = false
    )
    private Long orderId;

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(
            String paymentMethod,
            Integer amount,
            PaymentStatus status,
            Long orderId,
            PaymentProvider provider,
            String providerPaymentId
    ) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
        this.orderId = orderId;
        this.provider = provider;
        this.providerPaymentId = providerPaymentId;
    }

    public static Payment create(
            String paymentMethod,
            Integer amount,
            Long orderId,
            PaymentProvider provider
    ) {
        return Payment.builder()
                .paymentMethod(paymentMethod)
                .amount(amount)
                .status(PaymentStatus.READY)
                .orderId(orderId)
                .provider(provider)
                .build();
    }

    public boolean approve(String providerPaymentId, Integer approveAmount, LocalDateTime approvedAt) {
        if (providerPaymentId == null || providerPaymentId.isBlank() || approvedAt == null) {
            throw new BusinessException(ErrorCode.PAYMENT_CONFIRM_FAIL);
        }
        if (!this.amount.equals(approveAmount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (this.status == PaymentStatus.PAID) {
            if (!providerPaymentId.equals(this.providerPaymentId)) {
                throw new BusinessException(ErrorCode.PAYMENT_REQUEST_ERROR);
            }
            return false;
        }
        if (this.status != PaymentStatus.APPROVING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_IN_PROGRESS);
        }
        this.providerPaymentId = providerPaymentId;
        this.paidAt = approvedAt;
        this.status = PaymentStatus.PAID;
        return true;
    }

    public boolean cancel(
            String providerPaymentId,
            Integer canceledAmount,
            LocalDateTime canceledAt
    ) {
        if (canceledAt == null) {
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_FAIL);
        }
        if (!this.amount.equals(canceledAmount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (this.status == PaymentStatus.CANCELED) {
            if (!this.providerPaymentId.equals(providerPaymentId)) {
                throw new BusinessException(ErrorCode.PAYMENT_REQUEST_ERROR);
            }
            return false;
        }
        if (this.status != PaymentStatus.CANCELLING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_CANCELABLE);
        }
        if (!this.providerPaymentId.equals(providerPaymentId)) {
            throw new BusinessException(ErrorCode.PAYMENT_REQUEST_ERROR);
        }
        this.canceledAt = canceledAt;
        this.status = PaymentStatus.CANCELED;
        return true;
    }

    public void startApproval(Integer amount) {
        validateAmount(amount);
        if (this.status == PaymentStatus.APPROVING) {
            return;
        }
        validateApprovable();
        this.status = PaymentStatus.APPROVING;
    }

    public void startCancellation() {
        if (this.status == PaymentStatus.CANCELLING) {
            return;
        }
        validateCancelable();
        this.status = PaymentStatus.CANCELLING;
    }

    public void fail() {
        if (this.status != PaymentStatus.APPROVING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_IN_PROGRESS);
        }
        this.status = PaymentStatus.FAILED;
    }

    public void revertCancellation() {
        if (this.status != PaymentStatus.CANCELLING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_CANCELABLE);
        }
        this.status = PaymentStatus.PAID;
    }

    // == 상태 검증 메서드 == //
    public void validateCancelable() {
        if (this.status == PaymentStatus.CANCELED) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
        }
        if (this.status != PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_CANCELABLE);
        }
    }

    public void validateApprovable() {
        if (this.status == PaymentStatus.PAID || this.status == PaymentStatus.CANCELED) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
        }
        if (this.status == PaymentStatus.FAILED) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_FAILED);
        }
        if (this.status != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_IN_PROGRESS);
        }
    }

    private void validateAmount(Integer amount) {
        if (!this.amount.equals(amount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }
}
