package com.commerce.pagopa.category.presentation;

import com.commerce.pagopa.category.application.CategoryService;
import com.commerce.pagopa.category.application.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.category.application.dto.response.CategoryTreeResponseDto;
import com.commerce.pagopa.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CATEGORY API", description = "카테고리 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "대분류(최상위) 카테고리 목록 조회", description = "depth=0인 대분류(최상위) 카테고리 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategorySimpleResponseDto>>> getRootCategories() {
        return ResponseEntity.ok(
                ApiResponse.ok(categoryService.findRootCategories())
        );
    }

    @Operation(summary = "특정 카테고리의 하위 카테고리 목록 조회", description = "depth=1 또는 2인 자식 카테고리를 조회합니다.")
    @GetMapping("/{categoryId}/children")
    public ResponseEntity<ApiResponse<CategoryTreeResponseDto>> getChildCategories(
            @PathVariable("categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(categoryService.findChildCategories(categoryId))
        );
    }

    @Operation(summary = "하위 카테고리 포함 전체 조회", description = "대분류 카테고리의 자식 카테고리를 포함하여 전체 카테고리 목록을 조회합니다.")
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponseDto>>> getCategoryTree(
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(categoryService.findCategoryTree())
        );
    }
}
