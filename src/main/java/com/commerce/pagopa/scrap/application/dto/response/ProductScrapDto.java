package com.commerce.pagopa.scrap.application.dto.response;

import com.commerce.pagopa.product.application.dto.response.ProductImageResponseDto;
import com.commerce.pagopa.product.domain.model.Product;

import java.util.List;

public record ProductScrapDto(
        Long collectionId,
        Long id,
        String productName,
        String description,
        Integer price,
        Integer stockQuantity,
        String status,
        Long categoryId,
        Long sellerId,
        List<ProductImageResponseDto> productImages
) implements ScrapCollectionItem {

    public static ProductScrapDto from(Long collectionId, Product product) {
        return new ProductScrapDto(
                collectionId,
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus().getDescription(),
                product.getCategory().getId(),
                product.getSeller().getId(),
                product.getImages().stream()
                        .map(ProductImageResponseDto::from)
                        .toList()
        );
    }
}
