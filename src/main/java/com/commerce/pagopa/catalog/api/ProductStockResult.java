package com.commerce.pagopa.catalog.api;

public record ProductStockResult(
        Long productId,
        String productName,
        int unitPrice,
        int requestedQuantity
) {
}
