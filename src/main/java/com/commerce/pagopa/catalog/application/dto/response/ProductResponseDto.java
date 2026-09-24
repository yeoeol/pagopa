package com.commerce.pagopa.catalog.application.dto.response;

import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.catalog.domain.ProductStatus;
import com.commerce.pagopa.global.response.StatusResponseDto;

import java.util.List;

public record ProductResponseDto(
        Long productId,
        String productName,
        String description,
        Integer price,
        int stockQuantity,
        StatusResponseDto<ProductStatus> status,
        CategorySimpleResponseDto category,
        Long sellerId,
        List<ProductImageResponseDto> productImages
) {
    public static ProductResponseDto from(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                StatusResponseDto.from(product.getStatus()),
                CategorySimpleResponseDto.from(product.getCategory()),
                product.getUserId(),
                product.getImages().stream()
                        .map(ProductImageResponseDto::from)
                        .toList()
        );
    }
}
