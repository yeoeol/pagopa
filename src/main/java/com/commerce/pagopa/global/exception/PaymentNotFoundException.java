package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class PaymentNotFoundException extends BusinessException {
    public PaymentNotFoundException() {
        super(ErrorCode.PAYMENT_NOT_FOUND);
    }
}
