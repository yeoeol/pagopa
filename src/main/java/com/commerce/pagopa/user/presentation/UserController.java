package com.commerce.pagopa.user.presentation;

import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.user.application.UserService;
import com.commerce.pagopa.user.application.dto.request.UserUpdateRequestDto;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "USER API", description = "사용자 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "사용자 본인의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getInfo(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(userService.find(userId))
        );
    }

    @Operation(summary = "내 정보 수정", description = "사용자 본인의 정보를 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateInfo(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody UserUpdateRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(userService.update(userId, requestDto))
        );
    }
}
