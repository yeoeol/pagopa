package com.commerce.pagopa.seller.presentation;

import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.product.application.dto.response.ProductResponseDto;
import com.commerce.pagopa.seller.application.SellerProductService;
import com.commerce.pagopa.seller.application.dto.product.request.ProductAddStockRequestDto;
import com.commerce.pagopa.seller.application.dto.product.request.ProductRegisterRequestDto;
import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "SELLER PRODUCT API", description = "판매자 - 상품 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller/products")
public class SellerProductController {

    private final SellerProductService sellerProductService;

    @Operation(summary = "판매자 상품 목록 조회", description = "판매자 본인 상품 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getSellerProducts(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @ParameterObject @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(sellerProductService.findAll(userId, pageable))
        );
    }

    @Operation(summary = "판매자 상품 상세 조회", description = "판매자 본인의 특정 상품을 조회합니다.")
    @GetMapping("/{id}")
    @PreAuthorize("@sellerProductOwnerValidator.isOwner(#productId, principal.userId)")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getSellerProduct(
            @PathVariable("id") Long productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(sellerProductService.find(productId))
        );
    }

    @Operation(summary = "판매자 상품 등록", description = "판매자 상품을 등록합니다.")
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

    @Operation(summary = "판매자 상품 재고 추가", description = "특정 판매자 상품의 재고를 추가합니다.")
    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ApiResponse<ProductResponseDto>> addStock(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductAddStockRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(sellerProductService.addStock(productId, requestDto))
        );
    }
}
