package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class OrderCannotPayException extends BusinessException {
    public OrderCannotPayException() {
        super(ErrorCode.ORDER_CANNOT_PAY);
    }
}
