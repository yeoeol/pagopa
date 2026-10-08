package com.commerce.pagopa.catalog.api;

public record ProductSummary(Long productId, String productName, String description, Integer price, int stockQuantity,
        String status, Long sellerId) {
}
