package com.commerce.pagopa.global.seeder;

import net.datafaker.Faker;

import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

@Profile("local")
@Order(2)
@Component
@RequiredArgsConstructor
class UserSeeder implements Seeder {

    private final JdbcTemplate jdbc;
    private final Faker faker;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "user";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + name(), Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        int total = props.counts().users();
        String sql = """
                INSERT INTO users(
                    provider,
                    provider_id,
                    name,
                    email,
                    profile_image_url,
                    status,
                    status_changed_at,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        Timestamp now = Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 0, 0));

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            ps.setString(1, "LOCAL_TEST");
            ps.setString(2, UUID.nameUUIDFromBytes(("seed-provider-" + i).getBytes()).toString());
            ps.setString(3, "%s_%d".formatted(faker.name().firstName(), i));
            ps.setString(4, faker.internet().emailAddress("seed_user_%d".formatted(i)));
            ps.setString(5, i % 4 == 0 ? null : "https://picsum.photos/seed/u%d/200".formatted(i));

            // 95% ACTIVE, 3% WITHDRAWN, 2% BANNED
            int r = i % 100;
            String status = r < 95 ? "ACTIVE" : (r < 98 ? "WITHDRAWN" : "BANNED");
            ps.setString(6, status);

            // 과거 1년 내 랜덤
            ps.setTimestamp(7, Timestamp.from(faker.timeAndDate().past(365, TimeUnit.DAYS)));

            ps.setTimestamp(8, now);
            ps.setTimestamp(9, now);
        });
    }
}
