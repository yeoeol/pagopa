package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class NotProductOwnerException extends BusinessException {
    public NotProductOwnerException() {
        super(ErrorCode.NOT_PRODUCT_OWNER);
    }
}
