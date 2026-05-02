package com.commerce.pagopa.product.application.dto.response;

import com.commerce.pagopa.product.domain.model.ProductImage;

public record ProductImageResponseDto(
        String imageUrl,
        int displayOrder,
        boolean isThumbnail
) {
    public static ProductImageResponseDto from(ProductImage productImage) {
        return new ProductImageResponseDto(
                productImage.getImageUrl(),
                productImage.getDisplayOrder(),
                productImage.isThumbnail()
        );
    }
}
