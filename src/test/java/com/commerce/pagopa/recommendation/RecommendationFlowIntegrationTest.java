package com.commerce.pagopa.recommendation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.commerce.pagopa.catalog.api.ProductApi;
import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.ordering.event.OrderConfirmed;
import com.commerce.pagopa.recommendation.application.RecommendationQueryService;
import com.commerce.pagopa.recommendation.application.command.ProductSearchProjectionCommand;
import com.commerce.pagopa.recommendation.domain.InterestType;
import com.commerce.pagopa.recommendation.domain.RecommendationInterest;
import com.commerce.pagopa.recommendation.domain.RecommendationProjectionRepository;
import com.commerce.pagopa.support.testcontainers.TestcontainersConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

@ApplicationModuleTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfig.class)
class RecommendationFlowIntegrationTest {

    @Autowired
    RecommendationProjectionRepository recommendationProjectionRepository;

    @Autowired
    RecommendationQueryService recommendationQueryService;

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @MockitoBean
    ProductApi productApi;

    @AfterEach
    void deleteRecommendationData() {
        jdbcTemplate.update(
                "DELETE FROM recommendation_interest",
                Map.of()
        );

        jdbcTemplate.update(
                "DELETE FROM recommendation_event",
                Map.of()
        );

        jdbcTemplate.update(
                "DELETE FROM EVENT_PUBLICATION",
                Map.of()
        );
    }

    @Test
    void search_event_is_projected_and_used_for_recommendation(Scenario scenario) {
        UUID eventId = UUID.randomUUID();
        Long userId = 10_001L;
        String keyword = "keyboard";
        LocalDateTime searchedAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        ProductSummary keyboard = activeProduct(100L);

        given(
                productApi.findCandidatesByKeyword(
                        keyword,
                        List.of(),
                        1
                )
        ).willReturn(List.of(keyboard));

        ProductSearchProjectionCommand event = new ProductSearchProjectionCommand(
                eventId,
                userId,
                keyword,
                searchedAt
        );

        scenario.publish(event)
                .andWaitAtMost(Duration.ofSeconds(5))
                .andWaitForStateChange(
                        () -> recommendationProjectionRepository.findTopInterests(
                                userId,
                                InterestType.KEYWORD,
                                10
                        ),
                        interests -> interests.size() == 1
                )
                .andVerify(interests -> {
                    assertThat(interests).singleElement()
                            .satisfies(interest -> {
                                assertThat(interest.type()).isEqualTo(InterestType.KEYWORD);
                                assertThat(interest.interestKey()).isEqualTo(keyword);
                                assertThat(interest.score()).isEqualTo(1);
                            });
                    assertThat(
                            recommendationQueryService.getTopProducts(
                                    userId,
                                    1
                            )
                    ).containsExactly(keyboard);
                });
    }

    @Test
    void order_event_is_projected_and_products_are_recommended(Scenario scenario) {
        UUID eventId = UUID.randomUUID();
        Long orderId = 20_001L;
        Long userId = 10_002L;
        LocalDateTime confirmedAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        ProductSummary product10 = activeProduct(10L);
        ProductSummary product20 = activeProduct(20L);

        given(
                productApi.findAllByIds(
                        List.of(
                                10L,
                                20L
                        )
                )
        ).willReturn(
                Map.of(
                        10L,
                        product10,
                        20L,
                        product20
                )
        );

        OrderConfirmed event = new OrderConfirmed(
                eventId,
                orderId,
                userId,
                List.of(
                        10L,
                        20L
                ),
                confirmedAt
        );

        scenario.publish(event)
                .andWaitAtMost(Duration.ofSeconds(5))
                .andWaitForStateChange(
                        () -> recommendationProjectionRepository.findTopInterests(
                                userId,
                                InterestType.PRODUCT,
                                10
                        ),
                        interests -> interests.size() == 2
                )
                .andVerify(interests -> {
                    assertThat(interests).extracting(
                            RecommendationInterest::interestKey,
                            RecommendationInterest::score
                    )
                            .containsExactlyInAnyOrder(
                                    tuple(
                                            "10",
                                            5
                                    ),
                                    tuple(
                                            "20",
                                            5
                                    )
                            );

                    assertThat(
                            recommendationQueryService.getTopProducts(
                                    userId,
                                    2
                            )
                    ).containsExactly(
                            product10,
                            product20
                    );
                });
    }

    private ProductSummary activeProduct(Long productId) {
        return new ProductSummary(
                productId,
                "product-" + productId,
                "description",
                10_000,
                10,
                "ACTIVE",
                100L
        );
    }
}
