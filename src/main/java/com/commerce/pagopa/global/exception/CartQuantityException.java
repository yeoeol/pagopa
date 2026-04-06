package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class CartQuantityException extends BusinessException {
    public CartQuantityException() {
        super(ErrorCode.CART_QUANTITY);
    }
}
