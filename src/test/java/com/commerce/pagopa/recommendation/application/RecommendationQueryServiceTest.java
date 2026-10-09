package com.commerce.pagopa.recommendation.application;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commerce.pagopa.catalog.api.ProductApi;
import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.recommendation.domain.InterestType;
import com.commerce.pagopa.recommendation.domain.RecommendationInterest;
import com.commerce.pagopa.recommendation.domain.RecommendationProjectionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecommendationQueryServiceTest {

    @Mock
    ProductApi productApi;

    @Mock
    RecommendationProjectionRepository recommendationProjectionRepository;

    @InjectMocks
    RecommendationQueryService recommendationQueryService;

    /** PRODUCT가 limit을 채우면 KEYWORD와 기본 상품 API를 호출 X */
    @Test
    void product_interests_fill_limit_then_skips_keyword_and_default() {
        Long userId = 1L;
        int limit = 3;
        LocalDateTime occurredAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        List<RecommendationInterest> productInterests = List.of(
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "10",
                        15,
                        occurredAt
                ),
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "20",
                        10,
                        occurredAt
                ),
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "30",
                        5,
                        occurredAt
                )
        );

        given(
                recommendationProjectionRepository.findTopInterests(
                        eq(userId),
                        eq(InterestType.PRODUCT),
                        anyInt()
                )
        ).willReturn(productInterests);

        given(
                productApi.findAllByIds(
                        List.of(
                                10L,
                                20L,
                                30L
                        )
                )
        ).willReturn(
                Map.of(
                        10L,
                        activeProduct(10L),
                        20L,
                        activeProduct(20L),
                        30L,
                        activeProduct(30L)
                )
        );

        List<ProductSummary> result = recommendationQueryService.getTopProducts(
                userId,
                limit
        );

        assertThat(result).hasSize(limit)
                .extracting(ProductSummary::productId)
                .containsExactly(
                        10L,
                        20L,
                        30L
                );

        verify(
                recommendationProjectionRepository,
                never()
        ).findTopInterests(
                eq(userId),
                eq(InterestType.KEYWORD),
                anyInt()
        );

        verify(
                productApi,
                never()
        ).findCandidatesByKeyword(
                anyString(),
                anyCollection(),
                anyInt()
        );

        verify(
                productApi,
                never()
        ).findDefaultProducts(
                anyCollection(),
                anyInt()
        );
    }

    /** findAllByIds() Map 순서와 무관하게 관심도 순서로 반환 */
    @Test
    void returns_products_in_interest_order_regardless_of_map_order() {
        Long userId = 1L;
        int limit = 5;
        LocalDateTime occurredAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        List<RecommendationInterest> productInterests = List.of(
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "10",
                        15,
                        occurredAt
                ),
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "20",
                        10,
                        occurredAt
                ),
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "30",
                        5,
                        occurredAt
                ),
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "40",
                        3,
                        occurredAt
                ),
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "50",
                        1,
                        occurredAt
                )
        );

        given(
                recommendationProjectionRepository.findTopInterests(
                        eq(userId),
                        eq(InterestType.PRODUCT),
                        anyInt()
                )
        ).willReturn(productInterests);

        Map<Long, ProductSummary> productsById = new LinkedHashMap<>();
        productsById.put(
                30L,
                activeProduct(30L)
        );
        productsById.put(
                10L,
                activeProduct(10L)
        );
        productsById.put(
                50L,
                activeProduct(50L)
        );
        productsById.put(
                20L,
                activeProduct(20L)
        );
        productsById.put(
                40L,
                activeProduct(40L)
        );

        given(
                productApi.findAllByIds(
                        List.of(
                                10L,
                                20L,
                                30L,
                                40L,
                                50L
                        )
                )
        ).willReturn(productsById);

        List<ProductSummary> result = recommendationQueryService.getTopProducts(
                userId,
                limit
        );

        assertThat(result).hasSize(limit)
                .extracting(ProductSummary::productId)
                .containsExactly(
                        10L,
                        20L,
                        30L,
                        40L,
                        50L
                );
    }

    /** 상위 PRODUCT가 판매 불가여도 차순위 판매 가능 PRODUCT를 선택 */
    @Test
    void select_sellable_product_although_top_product_is_not_sellable() {
        Long userId = 1L;
        int limit = 2;
        LocalDateTime occurredAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        List<RecommendationInterest> productInterests = List.of(
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "10",
                        15,
                        occurredAt
                ),
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "20",
                        10,
                        occurredAt
                ),
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "30",
                        5,
                        occurredAt
                )
        );

        given(
                recommendationProjectionRepository.findTopInterests(
                        eq(userId),
                        eq(InterestType.PRODUCT),
                        anyInt()
                )
        ).willReturn(productInterests);

        given(
                productApi.findAllByIds(
                        List.of(
                                10L,
                                20L,
                                30L
                        )
                )
        ).willReturn(
                Map.of(
                        10L,
                        inactiveProduct(10L),
                        20L,
                        activeProduct(20L),
                        30L,
                        activeProduct(30L)
                )
        );

        List<ProductSummary> result = recommendationQueryService.getTopProducts(
                userId,
                limit
        );

        assertThat(result).hasSize(limit)
                .extracting(ProductSummary::productId)
                .containsExactly(
                        20L,
                        30L
                );

        verify(
                recommendationProjectionRepository,
                never()
        ).findTopInterests(
                eq(userId),
                eq(InterestType.KEYWORD),
                anyInt()
        );

        verify(
                productApi,
                never()
        ).findCandidatesByKeyword(
                anyString(),
                anyCollection(),
                anyInt()
        );

        verify(
                productApi,
                never()
        ).findDefaultProducts(
                anyCollection(),
                anyInt()
        );
    }

    /** PRODUCT 부족분을 KEYWORD 후보로 채움 */
    @Test
    void fills_remaining_slots_with_keyword_candidates() {
        Long userId = 1L;
        int limit = 3;
        LocalDateTime occurredAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        List<RecommendationInterest> productInterests = List.of(
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "20",
                        10,
                        occurredAt
                ),
                new RecommendationInterest(
                        userId,
                        InterestType.PRODUCT,
                        "30",
                        5,
                        occurredAt
                )
        );

        List<RecommendationInterest> keyboardInterests = List
                .of(
                        new RecommendationInterest(
                                userId,
                                InterestType.KEYWORD,
                                "keyboard",
                                5,
                                occurredAt
                        )
                );

        given(
                recommendationProjectionRepository.findTopInterests(
                        eq(userId),
                        eq(InterestType.PRODUCT),
                        anyInt()
                )
        ).willReturn(productInterests);

        given(
                productApi.findAllByIds(
                        List.of(
                                20L,
                                30L
                        )
                )
        ).willReturn(
                Map.of(
                        20L,
                        activeProduct(20L),
                        30L,
                        activeProduct(30L)
                )
        );

        given(
                recommendationProjectionRepository.findTopInterests(
                        eq(userId),
                        eq(InterestType.KEYWORD),
                        anyInt()
                )
        ).willReturn(keyboardInterests);

        given(
                productApi.findCandidatesByKeyword(
                        "keyboard",
                        List.of(
                                20L,
                                30L
                        ),
                        1
                )
        ).willReturn(List.of(activeProduct(40L)));

        List<ProductSummary> result = recommendationQueryService.getTopProducts(
                userId,
                limit
        );

        assertThat(result).hasSize(limit)
                .extracting(ProductSummary::productId)
                .containsExactly(
                        20L,
                        30L,
                        40L
                );

        verify(
                productApi,
                never()
        ).findDefaultProducts(
                anyCollection(),
                anyInt()
        );
    }

    /** KEYWORD가 채우면 기본 상품을 호출 X */
    @Test
    void keyword_candidates_fill_limit_then_skips_default_products() {
        Long userId = 1L;
        int limit = 3;
        LocalDateTime occurredAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        ProductSummary product10 = activeProduct(10L);
        ProductSummary product20 = activeProduct(20L);
        ProductSummary product30 = activeProduct(30L);

        given(
                recommendationProjectionRepository.findTopInterests(
                        eq(userId),
                        eq(InterestType.PRODUCT),
                        anyInt()
                )
        ).willReturn(List.of());

        given(
                recommendationProjectionRepository.findTopInterests(
                        eq(userId),
                        eq(InterestType.KEYWORD),
                        anyInt()
                )
        ).willReturn(
                List.of(
                        new RecommendationInterest(
                                userId,
                                InterestType.KEYWORD,
                                "keyboard",
                                5,
                                occurredAt
                        )
                )
        );

        given(
                productApi.findCandidatesByKeyword(
                        "keyboard",
                        List.of(),
                        limit
                )
        ).willReturn(
                List.of(
                        product10,
                        product20,
                        product30
                )
        );

        List<ProductSummary> result = recommendationQueryService.getTopProducts(
                userId,
                limit
        );

        assertThat(result).containsExactly(
                product10,
                product20,
                product30
        );

        verify(
                productApi,
                never()
        ).findDefaultProducts(
                anyCollection(),
                anyInt()
        );
    }

    /** PRODUCT·KEYWORD 중복 상품을 한 번만 반환 */
    @Test
    void returns_duplicate_product_only_once_across_product_and_keyword() {
        Long userId = 1L;
        int limit = 3;
        LocalDateTime occurredAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        ProductSummary product10 = activeProduct(10L);
        ProductSummary product20 = activeProduct(20L);
        ProductSummary product30 = activeProduct(30L);

        List<RecommendationInterest> productInterests = List
                .of(
                        new RecommendationInterest(
                                userId,
                                InterestType.PRODUCT,
                                "10",
                                10,
                                occurredAt
                        )
                );

        List<RecommendationInterest> keywordInterests = List.of(
                new RecommendationInterest(
                        userId,
                        InterestType.KEYWORD,
                        "keyboard",
                        5,
                        occurredAt
                ),
                new RecommendationInterest(
                        userId,
                        InterestType.KEYWORD,
                        "mouse",
                        3,
                        occurredAt
                )
        );

        given(
                recommendationProjectionRepository.findTopInterests(
                        eq(userId),
                        eq(InterestType.PRODUCT),
                        anyInt()
                )
        ).willReturn(productInterests);

        given(productApi.findAllByIds(List.of(10L))).willReturn(
                Map.of(
                        10L,
                        product10
                )
        );

        given(
                recommendationProjectionRepository.findTopInterests(
                        eq(userId),
                        eq(InterestType.KEYWORD),
                        anyInt()
                )
        ).willReturn(keywordInterests);

        /*
         * product10은 이미 PRODUCT 추천 결과에 들어 있다. KEYWORD API가 같은 상품을 반환해도 한 번만 포함되는지 확인한다.
         */
        given(
                productApi.findCandidatesByKeyword(
                        "keyboard",
                        List.of(10L),
                        2
                )
        ).willReturn(
                List.of(
                        product10,
                        product20
                )
        );

        /*
         * product10 중복이 제거됐기 때문에 남은 자리는 하나다.
         */
        given(
                productApi.findCandidatesByKeyword(
                        "mouse",
                        List.of(
                                10L,
                                20L
                        ),
                        1
                )
        ).willReturn(List.of(product30));

        List<ProductSummary> result = recommendationQueryService.getTopProducts(
                userId,
                limit
        );

        assertThat(result).containsExactly(
                product10,
                product20,
                product30
        );

        assertThat(result).extracting(ProductSummary::productId)
                .doesNotHaveDuplicates();

        verify(productApi).findCandidatesByKeyword(
                "keyboard",
                List.of(10L),
                2
        );

        verify(productApi).findCandidatesByKeyword(
                "mouse",
                List.of(
                        10L,
                        20L
                ),
                1
        );

        verify(
                productApi,
                never()
        ).findDefaultProducts(
                anyCollection(),
                anyInt()
        );
    }

    /** 후보 부족 시 기본 상품으로 남은 개수만 채움 */
    @Test
    void fills_remaining_slots_with_default_products_when_candidates_are_insufficient() {
        Long userId = 1L;
        int limit = 3;
        LocalDateTime occurredAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        ProductSummary product10 = activeProduct(10L);
        ProductSummary product20 = activeProduct(20L);
        ProductSummary product30 = activeProduct(30L);

        List<RecommendationInterest> productInterests = List
                .of(
                        new RecommendationInterest(
                                userId,
                                InterestType.PRODUCT,
                                "10",
                                10,
                                occurredAt
                        )
                );

        List<RecommendationInterest> keywordInterests = List
                .of(
                        new RecommendationInterest(
                                userId,
                                InterestType.KEYWORD,
                                "keyboard",
                                5,
                                occurredAt
                        )
                );

        given(
                recommendationProjectionRepository.findTopInterests(
                        eq(userId),
                        eq(InterestType.PRODUCT),
                        anyInt()
                )
        ).willReturn(productInterests);

        given(productApi.findAllByIds(List.of(10L))).willReturn(
                Map.of(
                        10L,
                        product10
                )
        );

        given(
                recommendationProjectionRepository.findTopInterests(
                        eq(userId),
                        eq(InterestType.KEYWORD),
                        anyInt()
                )
        ).willReturn(keywordInterests);

        given(
                productApi.findCandidatesByKeyword(
                        "keyboard",
                        List.of(10L),
                        2
                )
        ).willReturn(List.of(product20));

        given(
                productApi.findDefaultProducts(
                        List.of(
                                10L,
                                20L
                        ),
                        1
                )
        ).willReturn(List.of(product30));

        List<ProductSummary> result = recommendationQueryService.getTopProducts(
                userId,
                limit
        );

        assertThat(result).hasSize(limit)
                .extracting(ProductSummary::productId)
                .containsExactly(
                        10L,
                        20L,
                        30L
                );

        verify(productApi).findDefaultProducts(
                List.of(
                        10L,
                        20L
                ),
                1
        );
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

    private ProductSummary inactiveProduct(Long productId) {
        return new ProductSummary(
                productId,
                "product-" + productId,
                "description",
                10_000,
                10,
                "INACTIVE",
                100L
        );
    }
}
