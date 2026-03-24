package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class DuplicateNicknameException extends BusinessException {
    public DuplicateNicknameException() {
        super(ErrorCode.DUPLICATE_NICKNAME);
    }
}
