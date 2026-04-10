package com.commerce.pagopa.domain.category.controller;

import com.commerce.pagopa.domain.category.dto.request.ChildCategoryCreateRequestDto;
import com.commerce.pagopa.domain.category.dto.request.RootCategoryCreateRequestDto;
import com.commerce.pagopa.domain.category.dto.response.CategoryResponseDto;
import com.commerce.pagopa.domain.category.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.domain.category.dto.response.CategoryTreeResponseDto;
import com.commerce.pagopa.domain.category.service.CategoryService;
import com.commerce.pagopa.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    // 최상위 카테고리 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategorySimpleResponseDto>>> getRootCategories() {
        return ResponseEntity.ok(
                ApiResponse.ok(categoryService.findRootCategories())
        );
    }

    // 특정 카테고리의 하위 카테고리 목록 조회
    @GetMapping("/{categoryId}/children")
    public ResponseEntity<ApiResponse<CategoryTreeResponseDto>> getChildCategories(
            @PathVariable("categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(categoryService.findChildCategories(categoryId))
        );
    }

    @PostMapping("/root")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategorySimpleResponseDto>> createRoot(
            @Valid @RequestBody RootCategoryCreateRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(categoryService.createRoot(requestDto))
        );
    }

    @PostMapping("/child")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createChild(
            @Valid @RequestBody ChildCategoryCreateRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(categoryService.createChild(requestDto))
        );
    }
}