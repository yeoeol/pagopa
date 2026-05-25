package com.commerce.pagopa.mypage.application;

import com.commerce.pagopa.mypage.application.dto.response.MyPageOverviewResponseDto;
import com.commerce.pagopa.scrap.application.ScrapService;
import com.commerce.pagopa.user.application.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserService userService;
    private final ScrapService scrapService;

    @Transactional(readOnly = true)
    public MyPageOverviewResponseDto getOverview(Long userId) {
        return MyPageOverviewResponseDto.of(
                userService.find(userId),
                scrapService.countByUser(userId)
        );
    }
}
