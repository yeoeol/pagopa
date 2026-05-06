package com.commerce.pagopa.user.presentation;

import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.user.application.UserService;
import com.commerce.pagopa.user.application.dto.request.UserUpdateRequestDto;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "USER API", description = "사용자 관리 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserApiController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "사용자 본인의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(userService.find(userDetails.getUserId()))
        );
    }

    @Operation(summary = "내 정보 수정", description = "사용자 본인의 정보를 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateInfo(
            @RequestBody UserUpdateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(userService.update(userDetails.getUserId(), requestDto))
        );
    }
}
