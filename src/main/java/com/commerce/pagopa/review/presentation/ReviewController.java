package com.commerce.pagopa.review.presentation;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.review.application.ReviewService;
import com.commerce.pagopa.review.application.dto.request.ReviewCreateRequestDto;
import com.commerce.pagopa.review.application.dto.request.ReviewUpdateRequestDto;
import com.commerce.pagopa.review.application.dto.response.ProductReviewResponseDto;
import com.commerce.pagopa.review.application.dto.response.ReviewResponseDto;

@Tag(
        name = "REVIEW API",
        description = "리뷰 관리 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "리뷰 등록",
            description = "주문 물품에 대한 리뷰를 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDto>> review(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody ReviewCreateRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.ok(
                                reviewService.create(
                                        userId,
                                        requestDto
                                )
                        )
                );
    }

    @Operation(
            summary = "리뷰 수정",
            description = "작성한 리뷰를 수정합니다."
    )
    @PatchMapping("/{reviewId}")
    @PreAuthorize("@reviewOwnerValidator.isOwner(#reviewId, principal.userId)")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable("reviewId") Long reviewId,
            @Valid @RequestBody ReviewUpdateRequestDto requestDto
    ) {
        reviewService.update(
                reviewId,
                requestDto
        );
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(
            summary = "리뷰 삭제",
            description = "작성한 리뷰를 삭제합니다."
    )
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("@reviewOwnerValidator.isOwner(#reviewId, principal.userId)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("reviewId") Long reviewId) {
        reviewService.delete(reviewId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(
            summary = "상품별 리뷰 목록 조회",
            description = "특정 상품에 대한 리뷰 목록을 조회합니다."
    )
    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<List<ProductReviewResponseDto>>> getAllByProduct(
            @PathVariable("productId") Long productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        reviewService.findAllByProduct(productId)
                )
        );
    }
}
