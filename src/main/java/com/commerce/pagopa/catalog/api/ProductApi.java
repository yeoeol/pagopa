package com.commerce.pagopa.catalog.api;

import java.util.Collection;
import java.util.Map;

public interface ProductApi {
	boolean existsById(Long productId);

	Map<Long, ProductSummary> findAllByIds(Collection<Long> productIds);

	ProductSummary get(Long productId);

	ProductSummary register(ProductRegisterRequest request);

	ProductPageResponseDto findAllByUserId(
			Long sellerId,
			int pageSize,
			int pageNumber,
			String sort
	);
}
