package com.commerce.pagopa.global.exception;

import com.commerce.pagopa.global.response.ErrorCode;

public class ReviewNotPurchasedException extends BusinessException {
    public ReviewNotPurchasedException() {
        super(ErrorCode.REVIEW_NOT_PURCHASED);
    }
}
