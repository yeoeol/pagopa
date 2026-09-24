package com.commerce.pagopa.catalog.api;

import java.util.List;

public interface CartItemApi {
	List<CartItemSummary> findAllByIdInAndUserIdForUpdate(
			List<Long> cartItemIds,
			Long userId
	);


	void deleteAllByIdIn(List<Long> cartItemIds);
}
