package com.commerce.pagopa.user.domain.model.enums;

import com.commerce.pagopa.global.response.DescribedStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus implements DescribedStatus {
    ACTIVE("정상"),
    SUSPENDED("임시정지"),
    BANNED("영구정지"),
    WITHDRAWN("영구탈퇴"),
    ;

    private final String description;
}
