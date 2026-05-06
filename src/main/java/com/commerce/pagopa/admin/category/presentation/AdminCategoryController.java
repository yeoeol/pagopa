package com.commerce.pagopa.admin.category.presentation;

import com.commerce.pagopa.admin.category.application.dto.request.CategoryUpdateRequestDto;
import com.commerce.pagopa.admin.category.application.dto.response.CategoryResponseDto;
import com.commerce.pagopa.admin.category.application.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.admin.category.application.AdminCategoryService;
import com.commerce.pagopa.admin.category.application.dto.request.ChildCategoryCreateRequestDto;
import com.commerce.pagopa.admin.category.application.dto.request.RootCategoryCreateRequestDto;
import com.commerce.pagopa.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "ADMIN CATEGORY API", description = "관리자용 카테고리 관리 API")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @Operation(summary = "최상위 카테고리 생성", description = "depth=0인 최상위(루트) 카테고리를 생성합니다.")
    @PostMapping("/root")
    public ResponseEntity<ApiResponse<CategorySimpleResponseDto>> createRoot(
            @Valid @RequestBody RootCategoryCreateRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminCategoryService.createRoot(requestDto))
        );
    }

    @Operation(summary = "자식 카테고리 생성", description = "depth=1 또는 2인 자식 카테고리를 생성합니다.")
    @PostMapping("/child")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createChild(
            @Valid @RequestBody ChildCategoryCreateRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminCategoryService.createChild(requestDto))
        );
    }

    @Operation(summary = "카테고리 정보 수정", description = "카테고리의 정보를 수정합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CategorySimpleResponseDto>> update(
            @PathVariable("id") Long categoryId,
            @Valid @RequestBody CategoryUpdateRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminCategoryService.update(categoryId, requestDto))
        );
    }

    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("id") Long categoryId
    ) {
        adminCategoryService.delete(categoryId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
