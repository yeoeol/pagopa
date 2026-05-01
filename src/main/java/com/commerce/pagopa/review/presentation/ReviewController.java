package com.commerce.pagopa.review.presentation;

import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.review.application.ReviewService;
import com.commerce.pagopa.review.application.dto.request.ReviewCreateRequestDto;
import com.commerce.pagopa.review.application.dto.request.ReviewUpdateRequestDto;
import com.commerce.pagopa.review.application.dto.response.ReviewResponseDto;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("@orderProductOwnerValidator.isOwner(#requestDto.orderProductId(), principal.userId)")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> review(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewCreateRequestDto requestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(reviewService.create(userDetails.getUserId(), requestDto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.ok(reviewService.findAll())
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@reviewOwnerValidator.isOwner(#reviewId, principal.userId)")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable("id") Long reviewId,
            @Valid @RequestBody ReviewUpdateRequestDto requestDto
    ) {
        reviewService.update(reviewId, requestDto);
        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@reviewOwnerValidator.isOwner(#reviewId, principal.userId)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("id") Long reviewId
    ) {
        reviewService.delete(reviewId);
        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getAllByProduct(
            @PathVariable("id") Long productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(reviewService.findAllByProduct(productId))
        );
    }
}
