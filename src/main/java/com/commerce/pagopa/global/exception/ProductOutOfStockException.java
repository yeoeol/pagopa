package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class ProductOutOfStockException extends BusinessException {
    public ProductOutOfStockException() {
        super(ErrorCode.PRODUCT_OUT_OF_STOCK);
    }
}
