package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class CartNotFoundException extends BusinessException {
    public CartNotFoundException() {
        super(ErrorCode.ORDER_NOT_FOUND);
    }
}
