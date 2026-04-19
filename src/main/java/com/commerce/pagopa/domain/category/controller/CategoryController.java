package com.commerce.pagopa.domain.category.controller;

import com.commerce.pagopa.domain.category.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.domain.category.dto.response.CategoryTreeResponseDto;
import com.commerce.pagopa.domain.category.service.CategoryService;
import com.commerce.pagopa.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponseDto>>> getAllCategories(
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(categoryService.findCategories())
        );
    }
}