package com.commerce.pagopa.domain.product.controller;

import com.commerce.pagopa.domain.product.dto.request.ProductRegisterRequestDto;
import com.commerce.pagopa.domain.product.dto.response.ProductResponseDto;
import com.commerce.pagopa.domain.product.service.ProductService;
import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> register(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProductRegisterRequestDto requestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(productService.register(userDetails.getUserId(), requestDto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.ok(productService.findAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(productService.find(id))
        );
    }
}
