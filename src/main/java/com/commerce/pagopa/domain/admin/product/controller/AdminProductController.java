package com.commerce.pagopa.domain.admin.product.controller;

import com.commerce.pagopa.domain.admin.product.service.AdminProductService;
import com.commerce.pagopa.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> active(@PathVariable("id") Long productId) {
        adminProductService.active(productId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> inactive(@PathVariable("id") Long productId) {
        adminProductService.inactive(productId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> soldOut(@PathVariable("id") Long productId) {
        adminProductService.soldOut(productId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> hidden(@PathVariable("id") Long productId) {
        adminProductService.hidden(productId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/{id")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long productId) {
        adminProductService.delete(productId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
