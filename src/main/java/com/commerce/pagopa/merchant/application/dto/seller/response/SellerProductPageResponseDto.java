package com.commerce.pagopa.merchant.application.dto.seller.response;

import java.util.List;

import com.commerce.pagopa.catalog.api.ProductSummary;

public record SellerProductPageResponseDto(
        List<ProductSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        int startPage,
        int endPage
) {
}
