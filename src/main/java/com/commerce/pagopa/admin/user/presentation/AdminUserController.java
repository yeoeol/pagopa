package com.commerce.pagopa.admin.user.presentation;

import com.commerce.pagopa.admin.user.application.dto.response.UserResponseDto;
import com.commerce.pagopa.admin.user.application.AdminUserService;
import com.commerce.pagopa.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "ADMIN USER API", description = "관리자 - 사용자 관리 API")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "사용자 목록 조회", description = "전체 사용자 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getUsers(
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminUserService.findAll(pageable)));
    }

    @Operation(summary = "사용자 정지", description = "사용자를 정지(ban)합니다.")
    @PatchMapping("/{id}/ban")
    public ResponseEntity<ApiResponse<Void>> ban(@PathVariable("id") Long userId) {
        adminUserService.ban(userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "사용자 정지 해제", description = "사용자를 정지 해제(unban)합니다.")
    @PatchMapping("/{id}/unban")
    public ResponseEntity<ApiResponse<Void>> unban(@PathVariable("id") Long userId) {
        adminUserService.unban(userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다.")
    @PatchMapping("/{id}/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdrawn(@PathVariable("id") Long userId) {
        adminUserService.withdraw(userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "판매자 승급", description = "사용자의 권한을 판매자로 승급합니다.")
    @PatchMapping("/{id}/seller")
    public ResponseEntity<ApiResponse<Void>> updateSellerRole(@PathVariable("id") Long userId) {
        adminUserService.updateSellerRole(userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
