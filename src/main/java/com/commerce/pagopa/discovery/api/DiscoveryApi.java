package com.commerce.pagopa.discovery.api;

public interface DiscoveryApi {
	void saveHistory(Long userId, String sessionId, String keyword);
}
