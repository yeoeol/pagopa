package com.commerce.pagopa.catalog.application.dto.response;

import com.commerce.pagopa.catalog.domain.ProductImage;

public record ProductImageResponseDto(String imageUrl, int displayOrder, boolean isThumbnail) {
    public static ProductImageResponseDto from(ProductImage productImage) {
        return new ProductImageResponseDto(productImage.getImageUrl(), productImage.getDisplayOrder(),
                productImage.isThumbnail());
    }
}
