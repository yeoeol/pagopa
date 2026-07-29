package com.commerce.pagopa.user.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("정상"),
    SUSPENDED("임시정지"),
    BANNED("영구정지"),
    WITHDRAWN("영구탈퇴"),
    ;

    private final String description;
}
