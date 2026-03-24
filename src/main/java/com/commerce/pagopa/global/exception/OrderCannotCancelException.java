package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class OrderCannotCancelException extends BusinessException {
    public OrderCannotCancelException() {
        super(ErrorCode.ORDER_CANNOT_CANCEL);
    }
}
