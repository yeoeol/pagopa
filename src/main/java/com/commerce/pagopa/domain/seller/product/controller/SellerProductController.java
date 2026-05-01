package com.commerce.pagopa.domain.seller.product.controller;

import com.commerce.pagopa.product.application.dto.response.ProductResponseDto;
import com.commerce.pagopa.domain.seller.product.dto.request.ProductRegisterRequestDto;
import com.commerce.pagopa.domain.seller.product.service.SellerProductService;
import com.commerce.pagopa.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@RequestMapping("/api/v1/seller/products")
public class SellerProductController {

    private final SellerProductService sellerProductService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getSellerProducts(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(sellerProductService.findAll(userId, pageable))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@productOwnerValidator.isOwner(#productId, principal.userId)")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getSellerProduct(
            @PathVariable("id") Long productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(sellerProductService.find(productId))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> register(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody ProductRegisterRequestDto requestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        sellerProductService.register(userId, requestDto))
                );
    }
}
