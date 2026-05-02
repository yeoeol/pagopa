package com.commerce.pagopa.admin.product.presentation;

import com.commerce.pagopa.admin.product.application.dto.request.ProductStatusChangeRequestDto;
import com.commerce.pagopa.admin.product.application.AdminProductService;
import com.commerce.pagopa.product.application.dto.response.ProductResponseDto;
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
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getProducts(
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminProductService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(
            @PathVariable("id") Long productId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminProductService.find(productId)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @PathVariable("id") Long productId,
            @Valid @RequestBody ProductStatusChangeRequestDto requestDto
    ) {
        adminProductService.changeStatus(productId, requestDto);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long productId) {
        adminProductService.delete(productId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
