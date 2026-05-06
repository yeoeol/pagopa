package com.commerce.pagopa.review.presentation;

import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.review.application.ReviewService;
import com.commerce.pagopa.review.application.dto.request.ReviewCreateRequestDto;
import com.commerce.pagopa.review.application.dto.request.ReviewUpdateRequestDto;
import com.commerce.pagopa.review.application.dto.response.ReviewResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "REVIEW API", description = "리뷰 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 등록", description = "주문 물품에 대한 리뷰를 등록합니다.")
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

    @Operation(summary = "리뷰 수정", description = "작성한 리뷰를 수정합니다.")
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

    @Operation(summary = "리뷰 삭제", description = "작성한 리뷰를 삭제합니다.")
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

    @Operation(summary = "상품별 리뷰 목록 조회", description = "특정 상품에 대한 리뷰 목록을 조회합니다.")
    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getAllByProduct(
            @PathVariable("id") Long productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(reviewService.findAllByProduct(productId))
        );
    }
}
