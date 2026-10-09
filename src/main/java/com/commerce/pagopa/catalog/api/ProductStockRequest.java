package com.commerce.pagopa.catalog.api;

public record ProductStockRequest(
        Long productId,
        int quantity
) {
}
