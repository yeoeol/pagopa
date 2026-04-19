package com.commerce.pagopa.domain.admin.review.controller;

import com.commerce.pagopa.domain.admin.review.service.AdminReviewService;
import com.commerce.pagopa.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/reviews")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long reviewId) {
        adminReviewService.delete(reviewId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
