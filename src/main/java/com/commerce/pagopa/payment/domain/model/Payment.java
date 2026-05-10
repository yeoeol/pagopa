package com.commerce.pagopa.payment.domain.model;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.payment.domain.model.enums.PaymentStatus;
import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cancelledAmount; // 누적 환불 금액 (부분 취소 합산)

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
        this.cancelledAmount = BigDecimal.ZERO;
        this.status = status;
    }

    public static Payment create(Order order) {
        return Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.READY) // 초기 상태: 결제 대기
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

    /**
     * 환불 누적 처리 가능 여부 검증
     *  상태: PAID 또는 PARTIAL_CANCELLED 만 허용
     *  금액: 양수, 그리고 (현재 누적 + 이번 취소액) ≤ 원 결제 금액
     */
    public void validateCancelable(BigDecimal cancelAmount) {
        if (this.status == PaymentStatus.FAILED) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_FAILED);
        }
        if (this.status == PaymentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
        }
        if (this.status != PaymentStatus.PAID && this.status != PaymentStatus.PARTIAL_CANCELLED) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_CANCELABLE);
        }
        if (cancelAmount == null || cancelAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    ErrorCode.PAYMENT_CANCEL_AMOUNT_INVALID,
                    "취소 금액은 0보다 커야 합니다."
            );
        }
        if (this.cancelledAmount.add(cancelAmount).compareTo(this.amount) > 0) {
            throw new BusinessException(
                    ErrorCode.PAYMENT_CANCEL_AMOUNT_INVALID,
                    "취소 금액이 잔여 환불 가능 금액을 초과합니다. (잔여=%s, 요청=%s)".formatted(
                            this.amount.subtract(this.cancelledAmount), cancelAmount)
            );
        }
    }

    public boolean isAmountMatched(BigDecimal amount) {
        return this.amount.compareTo(amount) == 0;
    }

    // 결제 승인 완료
    public void success(String paymentKey) {
        validateConfirmable();
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.PAID;
    }

    // 결제 실패
    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    /**
     * 환불 누적 취소 — 누적 환불 금액을 더하고, 그 결과에 따라 상태를 자동 결정
     *  cancelledAmount < amount → PARTIAL_CANCELLED
     *  cancelledAmount == amount → CANCELLED
     */
    public void cancel(BigDecimal cancelAmount) {
        validateCancelable(cancelAmount);
        this.cancelledAmount = this.cancelledAmount.add(cancelAmount);
        this.status = this.cancelledAmount.compareTo(this.amount) == 0
                ? PaymentStatus.CANCELLED
                : PaymentStatus.PARTIAL_CANCELLED;
    }

    /**
     * 미승인 결제 로컬 취소(스케줄러용) — 환불 누적 없음, 단순히 결제 흐름을 종료
     * 승인된(PAID) 결제는 환불 절차로만 취소 가능하므로 거부
     */
    public void cancelUnpaid() {
        if (this.status == PaymentStatus.PAID || this.status == PaymentStatus.PARTIAL_CANCELLED) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_CANCELABLE,
                    "승인된 결제는 환불 금액과 함께 cancel(amount)로 취소해야 합니다.");
        }
        this.status = PaymentStatus.CANCELLED;
    }

}
