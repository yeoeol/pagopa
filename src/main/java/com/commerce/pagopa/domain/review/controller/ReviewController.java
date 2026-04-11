package com.commerce.pagopa.domain.review.controller;

import com.commerce.pagopa.domain.review.dto.request.ReviewUpdateRequestDto;
import com.commerce.pagopa.domain.review.dto.request.ReviewCreateRequestDto;
import com.commerce.pagopa.domain.review.dto.response.ReviewResponseDto;
import com.commerce.pagopa.domain.review.service.ReviewService;
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
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDto>> review(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReviewCreateRequestDto requestDto
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
    @PreAuthorize("@orderOwnerValidator.isOwner(#reviewId, principal.userId)")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable("id") Long reviewId,
            @RequestBody ReviewUpdateRequestDto requestDto
    ) {
        reviewService.update(reviewId, requestDto);
        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@orderOwnerValidator.isOwner(#reviewId, principal.userId)")
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
