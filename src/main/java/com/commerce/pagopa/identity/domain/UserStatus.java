package com.commerce.pagopa.identity.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.response.DescribedStatus;

@Getter
@RequiredArgsConstructor
public enum UserStatus implements DescribedStatus {
    ACTIVE("정상"), SUSPENDED("임시정지"), BANNED("영구정지"), WITHDRAWN("영구탈퇴"),;

    private final String description;
}
