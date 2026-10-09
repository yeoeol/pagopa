package com.commerce.pagopa.recommendation.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.commerce.pagopa.catalog.event.ProductSearched;
import com.commerce.pagopa.ordering.event.OrderConfirmed;
import com.commerce.pagopa.recommendation.application.command.ProductSearchProjectionCommand;

@Slf4j
@Component
@RequiredArgsConstructor
class RecommendationEventListener {

    private final RecommendationProjectionService recommendationProjectionService;

    @ApplicationModuleListener
    void on(ProductSearched event) {
        if (event.userId() == null) {
            return;
        }

        log.info(
                "Received product search for {}",
                event.eventId()
        );
        recommendationProjectionService.project(
                new ProductSearchProjectionCommand(
                        event.eventId(),
                        event.userId(),
                        event.keyword(),
                        event.searchedAt()
                )
        );
        log.info(
                "Finished product search for {}",
                event.eventId()
        );
    }

    @ApplicationModuleListener
    void on(OrderConfirmed event) {
        log.info(
                "Received order confirm for {}",
                event.eventId()
        );
        recommendationProjectionService.project(event);
        log.info(
                "Finished order confirm for {}",
                event.eventId()
        );
    }
}
