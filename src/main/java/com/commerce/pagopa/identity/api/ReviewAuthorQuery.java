package com.commerce.pagopa.identity.api;

import java.util.Collection;
import java.util.Map;

public interface ReviewAuthorQuery {
	ReviewAuthorSummary findById(Long userId);

	Map<Long, ReviewAuthorSummary> findAllByIds(
			Collection<Long> userIds
	);
}
