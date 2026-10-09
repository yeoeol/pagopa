package com.commerce.pagopa.merchant.application;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.catalog.api.*;
import com.commerce.pagopa.merchant.application.dto.product.request.ProductAddStockRequestDto;
import com.commerce.pagopa.merchant.application.dto.product.request.ProductRegisterRequestDto;
import com.commerce.pagopa.merchant.application.dto.seller.response.SellerProductPageResponseDto;
import com.commerce.pagopa.merchant.domain.Seller;
import com.commerce.pagopa.merchant.domain.SellerRepository;

@Service
@RequiredArgsConstructor
public class SellerProductService {

    private final SellerRepository sellerRepository;
    private final ProductApi productApi;
    private final ProductStockApi productStockApi;

    @Transactional(readOnly = true)
    public SellerProductPageResponseDto findAll(Long userId, Pageable pageable) {
        Seller seller = sellerRepository.findByUserIdOrThrow(userId);

        ProductPageResponseDto response = productApi.findAllByUserId(
                seller.getId(),
                pageable.getPageSize(),
                pageable.getPageNumber(),
                pageable.getSort()
                        .toString()
        );

        return new SellerProductPageResponseDto(
                response.content(),
                response.page(),
                response.size(),
                response.totalElements(),
                response.totalPages(),
                response.first(),
                response.last(),
                response.startPage(),
                response.endPage()
        );
    }

    @Transactional(readOnly = true)
    public ProductSummary find(Long productId) {
        return productApi.get(productId);
    }

    @Transactional
    public ProductSummary register(Long userId, ProductRegisterRequestDto requestDto) {
        Seller seller = sellerRepository.findByUserIdOrThrow(userId);

        return productApi.register(
                new ProductRegisterRequest(
                        seller.getId(),
                        requestDto.categoryId(),
                        requestDto.name(),
                        requestDto.description(),
                        requestDto.price(),
                        requestDto.stockQuantity(),
                        requestDto.imageUrls()
                )
        );
    }

    @Transactional
    public ProductStockResult addStock(Long productId, ProductAddStockRequestDto requestDto) {
        List<ProductStockResult> results = productStockApi
                .restoreStocks(
                        List.of(
                                new ProductStockRequest(
                                        productId,
                                        requestDto.quantity()
                                )
                        )
                );
        return results.getFirst();
    }
}
