package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class SellerOrderNotFoundException extends BusinessException {
    public SellerOrderNotFoundException() {
        super(ErrorCode.SELLER_ORDER_NOT_FOUND);
    }
}
