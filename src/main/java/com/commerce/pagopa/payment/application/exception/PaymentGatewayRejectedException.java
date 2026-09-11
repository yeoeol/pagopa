package com.commerce.pagopa.payment.application.exception;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

/**
 * PG가 거래를 명확히 거절한 경우.
 * 결제 중간 상태를 최종 실패(또는 취소 철회)로 확정해도 된다.
 */
public class PaymentGatewayRejectedException extends BusinessException {

	public PaymentGatewayRejectedException(ErrorCode errorCode) {
		super(errorCode);
	}
}
