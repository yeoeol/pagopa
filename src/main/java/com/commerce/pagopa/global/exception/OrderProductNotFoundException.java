package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class OrderProductNotFoundException extends BusinessException {
    public OrderProductNotFoundException() {
        super(ErrorCode.ORDER_PRODUCT_NOT_FOUND);
    }
}
