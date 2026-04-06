package com.commerce.pagopa.domain.cart.controller;

import com.commerce.pagopa.domain.cart.dto.request.CartAddRequestDto;
import com.commerce.pagopa.domain.cart.dto.response.CartResponseDto;
import com.commerce.pagopa.domain.cart.service.CartService;
import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<ApiResponse<CartResponseDto>> addCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CartAddRequestDto requestDto,
            @RequestParam(required = false, defaultValue = "true") boolean isAdd
    ) {
        CartResponseDto response = cartService.addCart(userDetails.getUserId(), requestDto, isAdd);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartResponseDto>>> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<CartResponseDto> responses = cartService.findUserCart(userDetails.getUserId());
        return ResponseEntity.ok(
                ApiResponse.ok(responses)
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@cartOwnerValidator.isOwner(#cartId, principal.userId)")
    public ResponseEntity<ApiResponse<CartResponseDto>> updateQuantity(
            @PathVariable("id") Long cartId,
            @RequestParam boolean isAdd
    ) {
        CartResponseDto response = cartService.updateQuantity(cartId, isAdd);
        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@cartOwnerValidator.isOwner(#cartId, principal.userId)")
    public ResponseEntity<ApiResponse<Void>> deleteCart(
            @PathVariable("id") Long cartId
    ) {
        cartService.delete(cartId);
        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        cartService.deleteAll(userDetails.getUserId());
        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }
}