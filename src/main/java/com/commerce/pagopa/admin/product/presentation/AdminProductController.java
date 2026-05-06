package com.commerce.pagopa.admin.product.presentation;

import com.commerce.pagopa.admin.product.application.dto.request.ProductStatusChangeRequestDto;
import com.commerce.pagopa.admin.product.application.AdminProductService;
import com.commerce.pagopa.product.application.dto.response.ProductResponseDto;
import com.commerce.pagopa.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "ADMIN PRODUCT API", description = "관리자 - 상품 관리 API")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @Operation(summary = "상품 목록 조회", description = "관리자 페이지 상품 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getProducts(
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminProductService.findAll(pageable)));
    }

    @Operation(summary = "상품 상세 조회", description = "관리자 페이지 특정 상품을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(
            @PathVariable("id") Long productId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminProductService.find(productId)));
    }

    @Operation(summary = "상품 상태 변경", description = "상품 상태를 수정합니다.")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @PathVariable("id") Long productId,
            @Valid @RequestBody ProductStatusChangeRequestDto requestDto
    ) {
        adminProductService.changeStatus(productId, requestDto);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long productId) {
        adminProductService.delete(productId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
