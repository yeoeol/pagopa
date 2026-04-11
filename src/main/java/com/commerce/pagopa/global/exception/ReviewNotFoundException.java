package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class ReviewNotFoundException extends BusinessException {
    public ReviewNotFoundException() {
        super(ErrorCode.REVIEW_NOT_FOUND);
    }
}
