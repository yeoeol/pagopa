package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class UserNotFountException extends BusinessException {
    public UserNotFountException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
