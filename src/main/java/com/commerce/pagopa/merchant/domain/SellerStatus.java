package com.commerce.pagopa.merchant.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.response.DescribedStatus;

@Getter
@RequiredArgsConstructor
public enum SellerStatus implements DescribedStatus {
    PENDING("대기"), REJECTED("거부"), ACTIVE("정상"), SUSPENDED("임시정지"), BANNED("영구정지"), PAUSED("판매중지"), WITHDRAWN("영구탈퇴"),;

    private final String description;
}
