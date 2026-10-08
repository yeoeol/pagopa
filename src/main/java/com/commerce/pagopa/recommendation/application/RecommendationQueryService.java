package com.commerce.pagopa.recommendation.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.catalog.api.ProductApi;
import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.recommendation.domain.InterestType;
import com.commerce.pagopa.recommendation.domain.RecommendationInterest;
import com.commerce.pagopa.recommendation.domain.RecommendationProjectionRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationQueryService {

    private final RecommendationProjectionRepository recommendationProjectionRepository;
    private final ProductApi productApi;

    private static final int MAX_PRODUCT_INTEREST_CANDIDATES = 100;
    private static final int MAX_KEYWORD_INTERESTS = 5;

    public List<ProductSummary> getTopProducts(Long userId, int limit) {
        Map<Long, ProductSummary> result = new LinkedHashMap<>();

        addProductInterests(userId, limit, result);
        if (result.size() == limit) {
            return result.values()
                    .stream()
                    .toList();
        }

        addKeywordCandidates(userId, limit, result);
        if (result.size() == limit) {
            return result.values()
                    .stream()
                    .toList();
        }

        addDefaultProducts(limit, result);
        return result.values()
                .stream()
                .toList();
    }

    private void addProductInterests(Long userId, int limit, Map<Long, ProductSummary> result) {
        List<RecommendationInterest> interests = recommendationProjectionRepository.findTopInterests(userId,
                InterestType.PRODUCT, MAX_PRODUCT_INTEREST_CANDIDATES);

        List<Long> productIds = interests.stream()
                .map(RecommendationInterest::interestKey)
                .map(Long::valueOf)
                .toList();

        if (productIds.isEmpty()) {
            return;
        }

        Map<Long, ProductSummary> productMap = productApi.findAllByIds(productIds);

        for (Long productId : productIds) {
            ProductSummary product = productMap.get(productId);

            if (isRecommendable(product)) {
                result.putIfAbsent(productId, product);
            }

            if (result.size() == limit) {
                return;
            }
        }
    }

    private void addKeywordCandidates(Long userId, int limit, Map<Long, ProductSummary> result) {
        List<RecommendationInterest> interests = recommendationProjectionRepository.findTopInterests(userId,
                InterestType.KEYWORD, Math.min(limit, MAX_KEYWORD_INTERESTS));

        for (RecommendationInterest interest : interests) {
            int remaining = limit - result.size();
            if (remaining == 0) {
                return;
            }

            List<ProductSummary> candidates = productApi.findCandidatesByKeyword(interest.interestKey(),
                    List.copyOf(result.keySet()), remaining);

            candidates.forEach(product -> result.putIfAbsent(product.productId(), product));
        }
    }

    private void addDefaultProducts(int limit, Map<Long, ProductSummary> result) {
        int remaining = limit - result.size();
        if (remaining == 0) {
            return;
        }

        List<ProductSummary> defaults = productApi.findDefaultProducts(List.copyOf(result.keySet()),
                limit - result.size());

        defaults.forEach(product -> result.putIfAbsent(product.productId(), product));
    }

    private boolean isRecommendable(ProductSummary product) {
        return product != null && "ACTIVE".equals(product.status()) && product.stockQuantity() > 0;
    }
}
