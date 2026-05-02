package com.commerce.pagopa.admin.user.presentation;

import com.commerce.pagopa.admin.user.application.dto.response.UserResponseDto;
import com.commerce.pagopa.admin.user.application.AdminUserService;
import com.commerce.pagopa.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getUsers(
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminUserService.findAll(pageable)));
    }

    @PatchMapping("/{id}/ban")
    public ResponseEntity<ApiResponse<Void>> ban(@PathVariable("id") Long userId) {
        adminUserService.ban(userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PatchMapping("/{id}/unban")
    public ResponseEntity<ApiResponse<Void>> unban(@PathVariable("id") Long userId) {
        adminUserService.unban(userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PatchMapping("/{id}/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdrawn(@PathVariable("id") Long userId) {
        adminUserService.withdraw(userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PatchMapping("/{id}/seller")
    public ResponseEntity<ApiResponse<Void>> updateSellerRole(@PathVariable("id") Long userId) {
        adminUserService.updateSellerRole(userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
