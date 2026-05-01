package com.commerce.pagopa.domain.seller.product.dto.response;

import com.commerce.pagopa.product.domain.model.ProductImage;

public record ProductImageResponseDto(
        Long productImageId,
        String imageUrl,
        int displayOrder,
        boolean isThumbnail
) {
    public static ProductImageResponseDto from(ProductImage productImage) {
        return new ProductImageResponseDto(
                productImage.getId(),
                productImage.getImageUrl(),
                productImage.getDisplayOrder(),
                productImage.isThumbnail()
        );
    }
}
