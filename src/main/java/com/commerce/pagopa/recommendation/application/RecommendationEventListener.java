package com.commerce.pagopa.recommendation.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.commerce.pagopa.discovery.event.UserSearchRecorded;
import com.commerce.pagopa.ordering.event.OrderConfirmed;

@Slf4j
@Component
@RequiredArgsConstructor
class RecommendationEventListener {

    private final RecommendationProjectionService recommendationProjectionService;

    @ApplicationModuleListener
    void on(UserSearchRecorded event) {
        log.info("Received user search for {}", event.eventId());
        recommendationProjectionService.project(event);
        log.info("Finished user search for {}", event.eventId());
    }

    @ApplicationModuleListener
    void on(OrderConfirmed event) {
        log.info("Received order confirm for {}", event.eventId());
        recommendationProjectionService.project(event);
        log.info("Finished order confirm for {}", event.eventId());
    }
}
