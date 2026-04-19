package com.commerce.pagopa.domain.seller.order.dto.response;

import com.commerce.pagopa.domain.product.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponseDto(
        Long productId,
        String name,
        String description,
        BigDecimal price,
        BigDecimal discountPrice,
        int stock,
        String status,
        Long categoryId,
        Long sellerId,
        List<ProductImageResponseDto> productImages
) {
    public static ProductResponseDto from(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getDiscountPrice(),
                product.getStock(),
                product.getStatus().getDescription(),
                product.getCategory().getId(),
                product.getSeller().getId(),
                product.getImages().stream()
                        .map(ProductImageResponseDto::from)
                        .toList()
        );
    }
}
