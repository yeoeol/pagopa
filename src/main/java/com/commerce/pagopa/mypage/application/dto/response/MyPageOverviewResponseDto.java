package com.commerce.pagopa.mypage.application.dto.response;

import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

public record MyPageOverviewResponseDto(
        UserResponseDto profile,
        long scrapCount
) {
    public static MyPageOverviewResponseDto of(UserResponseDto profile, long scrapCount) {
        return new MyPageOverviewResponseDto(profile, scrapCount);
    }
}
