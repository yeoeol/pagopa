package com.commerce.pagopa.domain.admin.product.dto.response;

import com.commerce.pagopa.domain.product.entity.ProductImage;

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
