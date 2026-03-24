package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class ReviewAlreadyExistsException extends BusinessException {
    public ReviewAlreadyExistsException() {
        super(ErrorCode.REVIEW_ALREADY_EXISTS);
    }
}
