package com.commerce.pagopa.payment.application.exception;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

/**
 * 타임아웃·전송 오류 등 PG 결과가 불명확한 경우.
 * 중간 상태(APPROVING/CANCELLING)를 즉시 복구하지 않는다.
 */
public class PaymentGatewayIndeterminateException extends BusinessException {

	public PaymentGatewayIndeterminateException(ErrorCode errorCode) {
		super(errorCode);
	}

	public PaymentGatewayIndeterminateException(ErrorCode errorCode, String internalMessage) {
		super(errorCode, internalMessage);
	}
}
