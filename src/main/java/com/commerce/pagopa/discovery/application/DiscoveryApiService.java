package com.commerce.pagopa.discovery.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.discovery.api.DiscoveryApi;

@Service
@RequiredArgsConstructor
public class DiscoveryApiService implements DiscoveryApi {

    private final SearchHistoryService searchHistoryService;

    @Transactional
    public void saveHistory(Long userId, String sessionId, String keyword) {
        searchHistoryService.saveHistory(userId, sessionId, keyword);
    }
}
