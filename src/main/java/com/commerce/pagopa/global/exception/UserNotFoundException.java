package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
