package com.commerce.pagopa.recommendation.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.recommendation.application.command.OrderConfirmedCommand;
import com.commerce.pagopa.recommendation.application.command.ProductSearchProjectionCommand;
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
    public void project(ProductSearchProjectionCommand command) {
        String keyword = normalize(command.keyword());

        RecommendationEvent recommendationEvent = new RecommendationEvent(
                command.eventId(),
                RecommendationEventType.PRODUCT_SEARCHED,
                command.userId(),
                null,
                new SearchPayload(keyword),
                command.searchedAt()
        );

        if (!recommendationProjectionRepository.saveEventIfAbsent(recommendationEvent)) {
            return;
        }

        recommendationProjectionRepository.increaseInterest(
                command.userId(),
                InterestType.KEYWORD,
                keyword,
                1,
                command.searchedAt()
        );
    }

    @Transactional
    public void project(OrderConfirmedCommand command) {
        List<Long> productIds = command.productIds()
                .stream()
                .distinct()
                .toList();

        RecommendationEvent recommendationEvent = new RecommendationEvent(
                command.eventId(),
                RecommendationEventType.ORDER_CONFIRMED,
                command.userId(),
                command.orderId(),
                new OrderPayload(productIds),
                command.confirmedAt()
        );

        if (!recommendationProjectionRepository.saveEventIfAbsent(recommendationEvent)) {
            return;
        }

        productIds.forEach(
                productId -> recommendationProjectionRepository.increaseInterest(
                        command.userId(),
                        InterestType.PRODUCT,
                        String.valueOf(productId),
                        5,
                        command.confirmedAt()
                )
        );
    }
}
