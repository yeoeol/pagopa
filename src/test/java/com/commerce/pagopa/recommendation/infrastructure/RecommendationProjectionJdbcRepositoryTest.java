package com.commerce.pagopa.recommendation.infrastructure;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.commerce.pagopa.recommendation.domain.InterestType;
import com.commerce.pagopa.recommendation.domain.RecommendationEvent;
import com.commerce.pagopa.recommendation.domain.RecommendationEvent.OrderPayload;
import com.commerce.pagopa.recommendation.domain.RecommendationEvent.SearchPayload;
import com.commerce.pagopa.recommendation.domain.RecommendationEventType;
import com.commerce.pagopa.recommendation.domain.RecommendationInterest;
import com.commerce.pagopa.support.testcontainers.JacksonTestConfig;
import com.commerce.pagopa.support.testcontainers.TestcontainersConfig;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RecommendationProjectionJdbcRepository.class, TestcontainersConfig.class, JacksonTestConfig.class})
@Timeout(
        value = 30,
        unit = TimeUnit.SECONDS
)
class RecommendationProjectionJdbcRepositoryTest {

    @Autowired
    RecommendationProjectionJdbcRepository recommendationProjectionJdbcRepository;
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    DataSource dataSource;

    @BeforeEach
    void verify_mysql_database() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(
                    connection.getMetaData()
                            .getDatabaseProductName()
            ).isEqualToIgnoringCase("MySQL");
        }
    }

    /** 최초 저장은 true, 같은 이벤트를 재요청하면 false */
    @Test
    void must_fail_on_same_event_id() {
        UUID eventId = UUID.randomUUID();

        boolean mustSuccess = recommendationProjectionJdbcRepository
                .saveEventIfAbsent(
                        new RecommendationEvent(
                                eventId,
                                RecommendationEventType.USER_SEARCH_RECORDED,
                                1L,
                                null,
                                new SearchPayload("TEST"),
                                LocalDateTime.now()
                        )
                );
        boolean mustFail = recommendationProjectionJdbcRepository
                .saveEventIfAbsent(
                        new RecommendationEvent(
                                eventId,
                                RecommendationEventType.USER_SEARCH_RECORDED,
                                1L,
                                null,
                                new SearchPayload("TEST"),
                                LocalDateTime.now()
                        )
                );

        assertThat(mustSuccess).isTrue();
        assertThat(mustFail).isFalse();
    }

    /** 같은 주문을 서로 다른 event_id로 중복 처리 방지 */
    @Test
    void must_fail_on_different_event_id_and_same_order_id() {
        LocalDateTime occurredAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        RecommendationEvent firstEvent = new RecommendationEvent(
                UUID.randomUUID(),
                RecommendationEventType.ORDER_CONFIRMED,
                1L,
                100L,
                new OrderPayload(
                        List.of(
                                10L,
                                20L
                        )
                ),
                occurredAt
        );

        RecommendationEvent secondEvent = new RecommendationEvent(
                UUID.randomUUID(),
                RecommendationEventType.ORDER_CONFIRMED,
                1L,
                100L,
                new OrderPayload(
                        List.of(
                                10L,
                                20L
                        )
                ),
                occurredAt
        );

        assertThat(recommendationProjectionJdbcRepository.saveEventIfAbsent(firstEvent)).isTrue();

        assertThat(recommendationProjectionJdbcRepository.saveEventIfAbsent(secondEvent)).isFalse();

        assertThat(countRecommendationEvents()).isEqualTo(1);
    }

    /** 검색 이벤트는 aggregate_id가 없어도 각각 저장 */
    @Test
    void must_save_different_search_events_when_aggregate_id_is_null() {
        LocalDateTime occurredAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        RecommendationEvent firstEvent = new RecommendationEvent(
                UUID.randomUUID(),
                RecommendationEventType.USER_SEARCH_RECORDED,
                1L,
                null,
                new RecommendationEvent.SearchPayload("keyboard"),
                occurredAt
        );

        RecommendationEvent secondEvent = new RecommendationEvent(
                UUID.randomUUID(),
                RecommendationEventType.USER_SEARCH_RECORDED,
                1L,
                null,
                new RecommendationEvent.SearchPayload("mouse"),
                occurredAt.plusSeconds(1)
        );

        assertThat(recommendationProjectionJdbcRepository.saveEventIfAbsent(firstEvent)).isTrue();

        assertThat(recommendationProjectionJdbcRepository.saveEventIfAbsent(secondEvent)).isTrue();

        assertThat(countRecommendationEvents()).isEqualTo(2);
    }

    /** 관심도 점수가 누적되고 100에서 제한되는지 검증 */
    @Test
    void must_accumulate_interest_score_up_to_100() {
        LocalDateTime firstEventAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );
        LocalDateTime secondEventAt = firstEventAt.plusMinutes(1);

        recommendationProjectionJdbcRepository.increaseInterest(
                1L,
                InterestType.PRODUCT,
                "10",
                60,
                firstEventAt
        );

        recommendationProjectionJdbcRepository.increaseInterest(
                1L,
                InterestType.PRODUCT,
                "10",
                60,
                secondEventAt
        );

        List<RecommendationInterest> interests = recommendationProjectionJdbcRepository.findTopInterests(
                1L,
                InterestType.PRODUCT,
                10
        );

        assertThat(interests).singleElement()
                .satisfies(interest -> {
                    assertThat(interest.score()).isEqualTo(100);
                    assertThat(interest.lastEventAt()).isEqualTo(secondEventAt);
                });
    }

    /** 늦게 도착한 과거 이벤트가 시간값을 되돌리지 않는지 검증 */
    @Test
    void must_not_move_last_event_at_backwards() {
        LocalDateTime newerEventAt = LocalDateTime.of(
                2026,
                1,
                1,
                13,
                0
        );
        LocalDateTime olderEventAt = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        recommendationProjectionJdbcRepository.increaseInterest(
                1L,
                InterestType.PRODUCT,
                "10",
                5,
                newerEventAt
        );

        recommendationProjectionJdbcRepository.increaseInterest(
                1L,
                InterestType.PRODUCT,
                "10",
                5,
                olderEventAt
        );

        RecommendationInterest interest = recommendationProjectionJdbcRepository
                .findTopInterests(
                        1L,
                        InterestType.PRODUCT,
                        10
                )
                .getFirst();

        assertThat(interest.score()).isEqualTo(10);
        assertThat(interest.lastEventAt()).isEqualTo(newerEventAt);
    }

    /**
     * 추천 관심사 정렬과 limit 검증 정렬 규칙 1. 점수 높은 순 2. 마지막 이벤트 시각 최신순 3. 키 오름차순 4. 요청한 개수만
     * 반환
     */
    @Test
    void must_find_top_interests_in_ranking_order_with_limit() {
        LocalDateTime baseTime = LocalDateTime.of(
                2026,
                1,
                1,
                12,
                0
        );

        recommendationProjectionJdbcRepository.increaseInterest(
                1L,
                InterestType.PRODUCT,
                "A",
                5,
                baseTime
        );
        recommendationProjectionJdbcRepository.increaseInterest(
                1L,
                InterestType.PRODUCT,
                "B",
                5,
                baseTime.plusMinutes(1)
        );
        recommendationProjectionJdbcRepository.increaseInterest(
                1L,
                InterestType.PRODUCT,
                "C",
                10,
                baseTime
        );

        List<RecommendationInterest> result = recommendationProjectionJdbcRepository.findTopInterests(
                1L,
                InterestType.PRODUCT,
                2
        );

        assertThat(result).extracting(RecommendationInterest::interestKey)
                .containsExactly(
                        "C",
                        "B"
                );
    }

    private long countRecommendationEvents() {
        Long count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM recommendation_event
                        """,
                new MapSqlParameterSource(),
                Long.class
        );

        return count == null ? 0L : count;
    }
}
