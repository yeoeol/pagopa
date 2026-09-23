package com.commerce.pagopa.discovery.api;

import com.commerce.pagopa.discovery.application.SearchHistoryService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscoveryApi {

	private final SearchHistoryService searchHistoryService;

	public void saveHistory(Long userId, String sessionId, String keyword) {
		searchHistoryService.saveHistory(userId, sessionId, keyword);
	}
}
