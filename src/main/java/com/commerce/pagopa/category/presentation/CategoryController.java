package com.commerce.pagopa.category.presentation;

import com.commerce.pagopa.category.application.CategoryService;
import com.commerce.pagopa.category.application.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.category.application.dto.response.CategoryTreeResponseDto;
import com.commerce.pagopa.global.response.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "CATEGORY API", description = "카테고리 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "루트 카테고리 목록 조회", description = "루트(최상위) 카테고리 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategorySimpleResponseDto>>> getRootCategories() {
        return ResponseEntity.ok(
                ApiResponse.ok(categoryService.findRootCategories())
        );
    }

    @Operation(summary = "기준 카테고리의 직계 자식 카테고리 목록 조회", description = "기준 카테고리의 바로 한 단계 아래인 카테고리를 조회합니다.")
    @GetMapping("/{categoryId}/children")
    public ResponseEntity<ApiResponse<List<CategorySimpleResponseDto>>> getChildCategories(
            @PathVariable("categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(categoryService.getChildren(categoryId))
        );
    }

    @Operation(summary = "기준 카테고리의 하위 카테고리 목록 조회", description = "기준 카테고리의 모든 하위 카테고리를 조회합니다.")
    @GetMapping("/{categoryId}/tree")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponseDto>>> getCategoryTree(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(categoryService.getDescendants(categoryId))
        );
    }
}
