package com.commerce.pagopa.recommendation.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.discovery.event.UserSearchRecorded;
import com.commerce.pagopa.ordering.event.OrderConfirmed;
import com.commerce.pagopa.recommendation.domain.InterestType;
import com.commerce.pagopa.recommendation.domain.RecommendationEvent;
import com.commerce.pagopa.recommendation.domain.RecommendationEvent.OrderPayload;
import com.commerce.pagopa.recommendation.domain.RecommendationEvent.SearchPayload;
import com.commerce.pagopa.recommendation.domain.RecommendationEventType;
import com.commerce.pagopa.recommendation.domain.RecommendationProjectionRepository;

import static com.commerce.pagopa.global.util.StringUtil.normalize;

@Service
@RequiredArgsConstructor
public class RecommendationProjectionService {

    private final RecommendationProjectionRepository recommendationProjectionRepository;

    @Transactional
    public void project(UserSearchRecorded event) {
        String keyword = normalize(event.keyword());

        RecommendationEvent recommendationEvent = new RecommendationEvent(
                event.eventId(),
                RecommendationEventType.USER_SEARCH_RECORDED,
                event.userId(),
                null,
                new SearchPayload(keyword),
                event.searchedAt()
        );

        if (!recommendationProjectionRepository.saveEventIfAbsent(recommendationEvent)) {
            return;
        }

        recommendationProjectionRepository.increaseInterest(
                event.userId(),
                InterestType.KEYWORD,
                keyword,
                1,
                event.searchedAt()
        );
    }

    @Transactional
    public void project(OrderConfirmed event) {
        List<Long> productIds = event.productIds()
                .stream()
                .distinct()
                .toList();

        RecommendationEvent recommendationEvent = new RecommendationEvent(
                event.eventId(),
                RecommendationEventType.ORDER_CONFIRMED,
                event.userId(),
                event.orderId(),
                new OrderPayload(productIds),
                event.confirmedAt()
        );

        if (!recommendationProjectionRepository.saveEventIfAbsent(recommendationEvent)) {
            return;
        }

        productIds.forEach(
                productId -> recommendationProjectionRepository.increaseInterest(
                        event.userId(),
                        InterestType.PRODUCT,
                        String.valueOf(productId),
                        5,
                        event.confirmedAt()
                )
        );
    }
}
