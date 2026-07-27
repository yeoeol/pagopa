package com.commerce.pagopa.user.domain.model.enums;

public enum UserStatus {
    ACTIVE,
    WITHDRAWN,  // 탈퇴
    SUSPENDED,     // 임시 정지
    BANNED, // 영구 정지

    ;
}
