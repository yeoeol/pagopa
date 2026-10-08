package com.commerce.pagopa.catalog.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ProductApi {
    boolean existsById(Long productId);

    Map<Long, ProductSummary> findAllByIds(Collection<Long> productIds);

    ProductSummary get(Long productId);

    ProductSummary register(ProductRegisterRequest request);

    ProductPageResponseDto findAllByUserId(Long sellerId, int pageSize, int pageNumber, String sort);

    List<ProductSummary> findCandidatesByKeyword(String keyword, Collection<Long> excludedProductIds, int limit);

    List<ProductSummary> findDefaultProducts(Collection<Long> excludedProductIds, int limit);
}
