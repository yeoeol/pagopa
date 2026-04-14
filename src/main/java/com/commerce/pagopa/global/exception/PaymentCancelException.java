package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class PaymentCancelException extends BusinessException {
    public PaymentCancelException() {
        super(ErrorCode.PAYMENT_CANCEL);
    }
}
