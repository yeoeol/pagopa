package com.commerce.pagopa.merchant.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.response.DescribedStatus;

@Getter
@RequiredArgsConstructor
public enum VerificationStatus implements DescribedStatus {
    UNVERIFIED("인증되지 않음"), VERIFIED("인증됨"),;

    private final String description;
}
