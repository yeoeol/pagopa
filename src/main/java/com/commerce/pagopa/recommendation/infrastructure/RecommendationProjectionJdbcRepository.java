package com.commerce.pagopa.recommendation.infrastructure;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import tools.jackson.databind.json.JsonMapper;

import com.commerce.pagopa.recommendation.domain.InterestType;
import com.commerce.pagopa.recommendation.domain.RecommendationEvent;
import com.commerce.pagopa.recommendation.domain.RecommendationInterest;
import com.commerce.pagopa.recommendation.domain.RecommendationProjectionRepository;

@Repository
@RequiredArgsConstructor
public class RecommendationProjectionJdbcRepository implements RecommendationProjectionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    private static final String INSERT_EVENT = """
            INSERT INTO recommendation_event (
                event_id,
                event_type,
                user_id,
                aggregate_id,
                payload,
                occurred_at
            ) VALUES (
                :eventId,
                :eventType,
                :userId,
                :aggregateId,
                :payload,
                :occurredAt
            )
            """;

    private static final String UPSERT_INTEREST = """
            INSERT INTO recommendation_interest (
                user_id,
                interest_type,
                interest_key,
                score,
                last_event_at
            )
            VALUES (
                :userId,
                :interestType,
                :interestKey,
                LEAST(100, :weight),
                :occurredAt
            )
            ON DUPLICATE KEY UPDATE
                score = LEAST(100, score + :weight),
                last_event_at = GREATEST(last_event_at, :occurredAt),
                updated_at = CURRENT_TIMESTAMP
            """;

    private static final String SELECT_TOP_INTERESTS = """
            SELECT
                user_id,
                interest_type,
                interest_key,
                score,
                last_event_at
            FROM recommendation_interest
            WHERE user_id = :userId
              AND interest_type = :interestType
            ORDER BY
                score DESC,
                last_event_at DESC,
                interest_key ASC
            LIMIT :limit
            """;

    @Override
    public boolean saveEventIfAbsent(RecommendationEvent event) {
        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue(
                "eventId",
                event.eventId()
                        .toString()
        )
                .addValue(
                        "eventType",
                        event.eventType()
                                .getValue()
                )
                .addValue(
                        "userId",
                        event.userId()
                )
                .addValue(
                        "aggregateId",
                        event.aggregateId(),
                        Types.BIGINT
                )
                .addValue(
                        "payload",
                        jsonMapper.writeValueAsString(event.payload()),
                        Types.VARCHAR
                )
                .addValue(
                        "occurredAt",
                        event.occurredAt()
                );

        try {
            return jdbcTemplate.update(
                    INSERT_EVENT,
                    parameters
            ) == 1;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    @Override
    public void increaseInterest(
            Long userId,
            InterestType type,
            String interestKey,
            int weight,
            LocalDateTime occurredAt
    ) {
        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue(
                "userId",
                userId
        )
                .addValue(
                        "interestType",
                        type.name()
                )
                .addValue(
                        "interestKey",
                        interestKey
                )
                .addValue(
                        "weight",
                        weight
                )
                .addValue(
                        "occurredAt",
                        occurredAt
                );

        jdbcTemplate.update(
                UPSERT_INTEREST,
                parameters
        );
    }

    @Override
    public List<RecommendationInterest> findTopInterests(Long userId, InterestType type, int limit) {
        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue(
                "userId",
                userId
        )
                .addValue(
                        "interestType",
                        type.name()
                )
                .addValue(
                        "limit",
                        limit
                );

        return jdbcTemplate.query(
                SELECT_TOP_INTERESTS,
                parameters,
                (rs, rowNum) -> new RecommendationInterest(
                        rs.getLong("user_id"),
                        InterestType.valueOf(rs.getString("interest_type")),
                        rs.getString("interest_key"),
                        rs.getInt("score"),
                        rs.getObject(
                                "last_event_at",
                                LocalDateTime.class
                        )
                )
        );
    }
}
