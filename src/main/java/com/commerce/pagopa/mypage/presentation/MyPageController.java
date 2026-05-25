package com.commerce.pagopa.mypage.presentation;

import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.mypage.application.MyPageService;
import com.commerce.pagopa.mypage.application.dto.response.MyPageOverviewResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MYPAGE API", description = "마이페이지 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(summary = "마이페이지 개요 조회", description = "사용자 프로필과 스크랩 개수를 한 번에 조회합니다.")
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<MyPageOverviewResponseDto>> getOverview(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(myPageService.getOverview(userId))
        );
    }
}
