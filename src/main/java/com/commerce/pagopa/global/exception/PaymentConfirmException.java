package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class PaymentConfirmException extends BusinessException {
    public PaymentConfirmException() {
        super(ErrorCode.PAYMENT_CONFIRM_FAIL);
    }
}
