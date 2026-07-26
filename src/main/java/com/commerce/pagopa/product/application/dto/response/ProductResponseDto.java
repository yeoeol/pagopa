package com.commerce.pagopa.product.application.dto.response;

import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import java.util.List;

public record ProductResponseDto(
        Long productId,
        String productName,
        String description,
        Integer price,
        int stockQuantity,
        String status,
        UserResponseDto seller,
        List<ProductImageResponseDto> productImages
) {
    public static ProductResponseDto from(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus().getDescription(),
                UserResponseDto.from(product.getSeller()),
                product.getImages().stream()
                        .map(ProductImageResponseDto::from)
                        .toList()
        );
    }
}
