package com.commerce.pagopa.catalog.application;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.catalog.api.ProductApi;
import com.commerce.pagopa.catalog.api.ProductPageResponseDto;
import com.commerce.pagopa.catalog.api.ProductRegisterRequest;
import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.catalog.domain.*;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Service
@RequiredArgsConstructor
public class ProductApiService implements ProductApi {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private static final int PAGE_WINDOW_SIZE = 10;
    private static final int MIN_RECOMMENDATION_LIMIT = 1;
    private static final int MAX_RECOMMENDATION_LIMIT = 100;

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long productId) {
        return productRepository.existsById(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, ProductSummary> findAllByIds(Collection<Long> productIds) {
        Map<Long, ProductSummary> productSummary = new HashMap<>();

        List<Product> products = productRepository.findByIdIn(productIds);
        products.forEach(product -> productSummary.put(product.getId(), toSummary(product)));

        return productSummary;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSummary get(Long productId) {
        Product product = productRepository.findByIdOrThrow(productId);
        return toSummary(product);
    }

    @Override
    @Transactional
    public ProductSummary register(ProductRegisterRequest request) {
        Category category = categoryRepository.findByIdOrThrow(request.categoryId());

        Product product = Product.create(request.name(), request.description(), request.price(),
                request.stockQuantity(), category, request.sellerId());

        for (int i = 0; i < request.imageUrls()
                .size(); i++) {
            boolean isThumbnail = (i == 0);
            ProductImage productImage = ProductImage.create(request.imageUrls()
                    .get(i), i + 1, isThumbnail, product);
            product.addImage(productImage);
        }

        return toSummary(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponseDto findAllByUserId(Long userId, int pageSize, int pageNumber, String sort) {
        Page<Product> products = productRepository.findAllByUserId(userId,
                PageRequest.of(pageNumber, pageSize, Sort.by(sort)));

        int totalPages = products.getTotalPages();
        int currentPage = products.getNumber();
        int startPage = max(0, currentPage - PAGE_WINDOW_SIZE / 2);
        int endPage = min(max(totalPages - 1, 0), startPage + PAGE_WINDOW_SIZE - 1);
        startPage = max(0, endPage - PAGE_WINDOW_SIZE + 1);

        return new ProductPageResponseDto(products.getContent()
                .stream()
                .map(this::toSummary)
                .toList(), currentPage, products.getSize(), products.getTotalElements(), totalPages, products.isFirst(),
                products.isLast(), startPage, endPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummary> findCandidatesByKeyword(
            String keyword,
            Collection<Long> excludedProductIds,
            int limit
    ) {
        validateRecommendationLimit(limit);

        return productRepository.findRecommendationCandidatesByKeyword(keyword, excludedProductIds, limit)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummary> findDefaultProducts(Collection<Long> excludedProductIds, int limit) {
        validateRecommendationLimit(limit);

        return productRepository.findDefaultRecommendationProducts(excludedProductIds, limit)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private void validateRecommendationLimit(int limit) {
        if (limit < MIN_RECOMMENDATION_LIMIT || limit > MAX_RECOMMENDATION_LIMIT) {
            throw new BusinessException(ErrorCode.RECOMMENDATION_INVALID_LIMIT,
                    "limit must be between 1 and 100: " + limit);
        }
    }

    private ProductSummary toSummary(Product product) {
        return new ProductSummary(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                product.getStockQuantity(), product.getStatus()
                        .name(),
                product.getUserId());
    }
}
