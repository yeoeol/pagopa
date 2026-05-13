package com.commerce.pagopa.global.seeder;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Profile("local")
@Order(10)
@Component
@RequiredArgsConstructor
class SearchHistorySeeder implements Seeder {

    private final JdbcTemplate jdbc;
    private final Faker faker;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "search_histories";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM search_histories", Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        // user_id nullable - ACTIVE 사용자 풀 사용, 비로그인 분기는 NULL 채움
        List<Long> userIds = jdbc.queryForList(
                "SELECT user_id FROM users WHERE user_status = 'ACTIVE' ORDER BY user_id",
                Long.class);

        if (userIds.isEmpty()) {
            throw new IllegalStateException("user 없음");
        }

        int total = props.counts().searchHistories();
        int userSize = userIds.size();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        String sql = """
                INSERT INTO search_histories(user_id, session_id, keyword,
                                             last_searched_at, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            // 70% 로그인 / 30% 비로그인
            boolean loggedIn = i % 10 < 7;
            if (loggedIn) {
                ps.setLong(1, userIds.get(i % userSize));
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setNull(1, Types.BIGINT);
                // 비로그인 세션을 5개씩 묶어 동일 세션 내 검색 흐름 모사
                ps.setString(2, "sess_%d".formatted(i / 5));
            }

            ps.setString(3, faker.commerce().productName());
            ps.setTimestamp(4, Timestamp.from(faker.timeAndDate().past(30, TimeUnit.DAYS)));
            ps.setTimestamp(5, now);
            ps.setTimestamp(6, now);
        });
    }
}
