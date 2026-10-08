package com.commerce.pagopa.catalog.api;

import java.util.List;

public record ProductRegisterRequest(Long sellerId, Long categoryId, String name, String description, Integer price,
        Integer stockQuantity, List<String> imageUrls) {
}
