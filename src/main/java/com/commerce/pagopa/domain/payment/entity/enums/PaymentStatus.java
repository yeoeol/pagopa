package com.commerce.pagopa.domain.payment.entity.enums;

public enum PaymentStatus {
    READY,      // 결제 대기
    IN_PROGRESS, // 결제 진행 중 (사용자가 결제창 진입)
    PAID,       // 결제 완료 (승인됨)
    FAILED,     // 결제 실패
    CANCELLED   // 결제 취소
}
