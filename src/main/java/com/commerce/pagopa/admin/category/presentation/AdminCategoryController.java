package com.commerce.pagopa.admin.category.presentation;

import com.commerce.pagopa.admin.category.application.dto.request.CategoryUpdateRequestDto;
import com.commerce.pagopa.admin.category.application.dto.response.CategoryResponseDto;
import com.commerce.pagopa.admin.category.application.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.admin.category.application.AdminCategoryService;
import com.commerce.pagopa.admin.category.application.dto.request.ChildCategoryCreateRequestDto;
import com.commerce.pagopa.admin.category.application.dto.request.RootCategoryCreateRequestDto;
import com.commerce.pagopa.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CategorySimpleResponseDto>> update(
            @PathVariable("id") Long categoryId,
            @Valid @RequestBody CategoryUpdateRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminCategoryService.update(categoryId, requestDto))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("id") Long categoryId
    ) {
        adminCategoryService.delete(categoryId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
