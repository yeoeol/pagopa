package com.commerce.pagopa.domain.admin.category.controller;

import com.commerce.pagopa.domain.admin.category.dto.response.CategoryResponseDto;
import com.commerce.pagopa.domain.admin.category.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.domain.admin.category.service.AdminCategoryService;
import com.commerce.pagopa.domain.admin.category.dto.request.ChildCategoryCreateRequestDto;
import com.commerce.pagopa.domain.admin.category.dto.request.RootCategoryCreateRequestDto;
import com.commerce.pagopa.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @PostMapping("/root")
    public ResponseEntity<ApiResponse<CategorySimpleResponseDto>> createRoot(
            @Valid @RequestBody RootCategoryCreateRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminCategoryService.createRoot(requestDto))
        );
    }

    @PostMapping("/child")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createChild(
            @Valid @RequestBody ChildCategoryCreateRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminCategoryService.createChild(requestDto))
        );
    }
}
