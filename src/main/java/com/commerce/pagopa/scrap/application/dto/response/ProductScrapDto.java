package com.commerce.pagopa.scrap.application.dto.response;

import com.commerce.pagopa.product.application.dto.response.ProductImageResponseDto;

import java.math.BigDecimal;
import java.util.List;

public record ProductScrapDto(
        Long collectionId,
        Long id,
        String productName,
        String description,
        BigDecimal price,
        BigDecimal discountPrice,
        int stock,
        String status,
        Long categoryId,
        Long sellerId,
        List<ProductImageResponseDto> productImages
) implements ScrapCollectionItem {}
