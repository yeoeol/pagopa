package com.commerce.pagopa.recommendation.application;

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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.commerce.pagopa.discovery.event.UserSearchRecorded;
import com.commerce.pagopa.ordering.event.OrderConfirmed;
import com.commerce.pagopa.recommendation.domain.InterestType;
import com.commerce.pagopa.recommendation.domain.RecommendationInterest;
import com.commerce.pagopa.recommendation.infrastructure.RecommendationProjectionJdbcRepository;
import com.commerce.pagopa.support.testcontainers.JacksonTestConfig;
import com.commerce.pagopa.support.testcontainers.TestcontainersConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RecommendationProjectionService.class, RecommendationProjectionJdbcRepository.class,
        TestcontainersConfig.class, JacksonTestConfig.class})
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class RecommendationProjectionServiceTest {

    @Autowired
    RecommendationProjectionService recommendationProjectionService;
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    @MockitoSpyBean
    RecommendationProjectionJdbcRepository recommendationProjectionRepository;
    @Autowired
    DataSource dataSource;

    @BeforeEach
    void verify_mysql_database() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData()
                    .getDatabaseProductName()).isEqualToIgnoringCase("MySQL");
        }
    }

    /** 검색 이벤트 한 건 → KEYWORD 한 행 +1 */
    @Test
    void search_event_creates_one_keyword_interest_with_score_one() {
        UUID eventId = UUID.randomUUID();
        Long userId = 1L;
        String keyword = "keyboard";
        LocalDateTime searchedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        UserSearchRecorded event = new UserSearchRecorded(eventId, userId, keyword, searchedAt);
        recommendationProjectionService.project(event);

        List<RecommendationInterest> interests = findInterests(userId);

        assertThat(interests).singleElement()
                .satisfies(interest -> {
                    assertThat(interest.userId()).isEqualTo(userId);
                    assertThat(interest.type()).isEqualTo(InterestType.KEYWORD);
                    assertThat(interest.interestKey()).isEqualTo(keyword);
                    assertThat(interest.score()).isEqualTo(1);
                    assertThat(interest.lastEventAt()).isEqualTo(searchedAt);
                });
    }

    /** 상품 3개 주문 → PRODUCT 세 행 각각 +5 */
    @Test
    void order_with_three_products_creates_three_product_interests_with_score_five() {
        UUID eventId = UUID.randomUUID();
        Long orderId = 100L;
        Long userId = 1L;
        List<Long> productIds = List.of(10L, 20L, 30L);
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        OrderConfirmed event = new OrderConfirmed(eventId, orderId, userId, productIds, confirmedAt);
        recommendationProjectionService.project(event);

        List<RecommendationInterest> interests = findInterests(1L);

        assertThat(interests).hasSize(3);
        interests.forEach(interest -> {
            assertThat(interest.userId()).isEqualTo(1L);
            assertThat(interest.type()).isEqualTo(InterestType.PRODUCT);
            assertThat(interest.interestKey()).containsAnyOf("10", "20", "30");
            assertThat(interest.score()).isEqualTo(5);
            assertThat(interest.lastEventAt()).isEqualTo(confirmedAt);
        });
    }

    /** 중복 productId → 한 번만 반영 */
    @Test
    void duplicate_product_id_is_reflected_only_once() {
        UUID eventId = UUID.randomUUID();
        Long orderId = 100L;
        Long userId = 1L;
        List<Long> productIds = List.of(10L, 10L);
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        OrderConfirmed event = new OrderConfirmed(eventId, orderId, userId, productIds, confirmedAt);
        recommendationProjectionService.project(event);

        List<RecommendationInterest> interests = findInterests(userId);

        assertThat(interests).singleElement()
                .satisfies(interest -> {
                    assertThat(interest.userId()).isEqualTo(userId);
                    assertThat(interest.type()).isEqualTo(InterestType.PRODUCT);
                    assertThat(interest.interestKey()).isEqualTo("10");
                    assertThat(interest.score()).isEqualTo(5);
                    assertThat(interest.lastEventAt()).isEqualTo(confirmedAt);
                });
    }

    /** 중복 검색 이벤트 → 관심도 증가 없음 */
    @Test
    void duplicate_search_event_does_not_increase_keyword_score() {
        UUID eventId = UUID.randomUUID();
        Long userId = 1L;
        String keyword = "keyboard";
        LocalDateTime searchedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        UserSearchRecorded event = new UserSearchRecorded(eventId, userId, keyword, searchedAt);
        recommendationProjectionService.project(event);
        recommendationProjectionService.project(event);

        List<RecommendationInterest> interests = findInterests(userId);

        assertThat(interests).singleElement()
                .satisfies(interest -> {
                    assertThat(interest.type()).isEqualTo(InterestType.KEYWORD);
                    assertThat(interest.interestKey()).isEqualTo(keyword);
                    assertThat(interest.score()).isEqualTo(1);
                    assertThat(interest.lastEventAt()).isEqualTo(searchedAt);
                });
    }

    /** 중복 주문 확정 이벤트 → 관심도 증가 없음 */
    @Test
    void duplicate_order_event_does_not_increase_product_score() {
        UUID eventId = UUID.randomUUID();
        Long orderId = 100L;
        Long userId = 1L;
        List<Long> productIds = List.of(10L);
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        OrderConfirmed event = new OrderConfirmed(eventId, orderId, userId, productIds, confirmedAt);
        recommendationProjectionService.project(event);
        recommendationProjectionService.project(event);

        List<RecommendationInterest> interests = findInterests(userId);

        assertThat(interests).singleElement()
                .satisfies(interest -> {
                    assertThat(interest.userId()).isEqualTo(userId);
                    assertThat(interest.type()).isEqualTo(InterestType.PRODUCT);
                    assertThat(interest.interestKey()).isEqualTo("10");
                    assertThat(interest.score()).isEqualTo(5);
                    assertThat(interest.lastEventAt()).isEqualTo(confirmedAt);
                });
    }

    /** 관심도 UPSERT 실패 → 이벤트 INSERT도 롤백 */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void interest_upsert_failure_rolls_back_event_insert() {
        UUID eventId = UUID.randomUUID();
        Long userId = 999L;
        String keyword = "keyboard";
        LocalDateTime searchedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        UserSearchRecorded event = new UserSearchRecorded(eventId, userId, keyword, searchedAt);

        doThrow(new DataIntegrityViolationException("forced interest upsert failure"))
                .when(recommendationProjectionRepository)
                .increaseInterest(userId, InterestType.KEYWORD, keyword, 1, searchedAt);

        assertThatThrownBy(() -> recommendationProjectionService.project(event))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("forced interest upsert failure");

        assertThat(countEvents(eventId)).isZero();
        assertThat(findInterests(userId)).isEmpty();
    }

    private Long countEvents(UUID eventId) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM recommendation_event
                WHERE event_id = :eventId
                """, new MapSqlParameterSource().addValue("eventId", eventId.toString()), Long.class);
        return count == null ? 0L : count;
    }

    private List<RecommendationInterest> findInterests(Long userId) {
        return jdbcTemplate.query("""
                SELECT
                    user_id,
                    interest_type,
                    interest_key,
                    score,
                    last_event_at
                FROM recommendation_interest
                WHERE user_id=:userId
                """, new MapSqlParameterSource().addValue("userId", userId),
                (rs, rowNum) -> new RecommendationInterest(rs.getLong("user_id"),
                        InterestType.valueOf(rs.getString("interest_type")), rs.getString("interest_key"),
                        rs.getInt("score"), rs.getObject("last_event_at", LocalDateTime.class)));
    }
}
