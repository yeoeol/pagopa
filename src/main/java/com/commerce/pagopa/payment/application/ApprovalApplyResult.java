package com.commerce.pagopa.payment.application;

public enum ApprovalApplyResult {
    APPLIED,         // 현재 시도 결과를 이번에 반영함
    ALREADY_APPLIED, // 같은 시도 결과가 이미 반영됨
    STALE            // 이전 시도의 오래된 결과임
}
