package com.commerce.pagopa.payment.domain.model.enums;

public enum PaymentStatus {
    READY,             // 결제 대기
    IN_PROGRESS,       // 결제 진행 중 (사용자가 결제창 진입)
    PAID,              // 결제 완료 (승인됨)
    FAILED,            // 결제 실패
    PARTIAL_CANCELLED, // 부분 취소 (일부 SellerOrder만 취소되어 Toss 부분 환불됨, 추가 부분/전체 취소 가능)
    CANCELLED          // 결제 전체 취소
}
