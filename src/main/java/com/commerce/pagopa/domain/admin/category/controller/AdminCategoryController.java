package com.commerce.pagopa.domain.admin.category.controller;

import com.commerce.pagopa.domain.admin.category.dto.request.CategoryUpdateRequestDto;
import com.commerce.pagopa.domain.admin.category.dto.response.CategoryResponseDto;
import com.commerce.pagopa.domain.admin.category.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.domain.admin.category.dto.response.CategoryTreeResponseDto;
import com.commerce.pagopa.domain.admin.category.service.AdminCategoryService;
import com.commerce.pagopa.domain.admin.category.dto.request.ChildCategoryCreateRequestDto;
import com.commerce.pagopa.domain.admin.category.dto.request.RootCategoryCreateRequestDto;
import com.commerce.pagopa.global.response.ApiResponse;
import jakarta.validation.Valid;
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

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoryTreeResponseDto>>> getCategories(
            @PageableDefault(size = 10, page = 0, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminCategoryService.findCategories(pageable))
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
