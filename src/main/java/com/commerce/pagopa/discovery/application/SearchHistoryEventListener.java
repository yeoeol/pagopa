package com.commerce.pagopa.discovery.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.commerce.pagopa.discovery.event.ProductSearched;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchHistoryEventListener {

    private final SearchHistoryService searchHistoryService;

    @ApplicationModuleListener
    void on(ProductSearched event) {
        log.info("Received product search");
        searchHistoryService.saveHistory(
                event.userId(),
                event.sessionId(),
                event.keyword()
        );
        log.info("Finished product search");
    }
}
