package com.commerce.pagopa.product.application.dto.response;

import com.commerce.pagopa.category.application.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.model.enums.ProductStatus;
import com.commerce.pagopa.seller.application.dto.seller.response.SellerResponseDto;

import java.util.List;

public record ProductResponseDto(
        Long productId,
        String productName,
        String description,
        Integer price,
        int stockQuantity,
        StatusResponseDto<ProductStatus> status,
        CategorySimpleResponseDto category,
        SellerResponseDto seller,
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
                SellerResponseDto.from(product.getSeller()),
                product.getImages().stream()
                        .map(ProductImageResponseDto::from)
                        .toList()
        );
    }
}
