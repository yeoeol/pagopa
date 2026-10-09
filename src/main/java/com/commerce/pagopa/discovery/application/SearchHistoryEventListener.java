package com.commerce.pagopa.discovery.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.commerce.pagopa.catalog.event.ProductSearched;
import com.commerce.pagopa.discovery.application.command.ProductSearchHistoryCommand;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchHistoryEventListener {

    private final SearchHistoryService searchHistoryService;

    @ApplicationModuleListener
    void on(ProductSearched event) {
        log.info(
                "Received product search for {}",
                event.eventId()
        );
        searchHistoryService.saveHistory(
                new ProductSearchHistoryCommand(
                        event.userId(),
                        event.sessionId(),
                        event.keyword(),
                        event.searchedAt()
                )
        );
        log.info(
                "Finished product search for {}",
                event.eventId()
        );
    }
}
