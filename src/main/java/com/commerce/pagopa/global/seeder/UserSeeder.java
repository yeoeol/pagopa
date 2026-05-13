package com.commerce.pagopa.global.seeder;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Profile("local")
@Order(2)
@Component
@RequiredArgsConstructor
class UserSeeder implements Seeder {

    private static final String[] PROVIDERS = {"GOOGLE", "KAKAO", "NAVER"};

    private final JdbcTemplate jdbc;
    private final Faker faker;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "users";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        int total = props.counts().users();
        String sql = """
                INSERT INTO users(
                    email, nickname, profile_image,
                    provider, provider_id, role, user_status,
                    withdrawn_at, ban_end_date, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            // unique 제약: email/nickname은 인덱스로 충돌 회피
            ps.setString(1, "seed_user_%d@example.com".formatted(i));
            ps.setString(2, "%s_%d".formatted(faker.name().firstName(), i));
            ps.setString(3, i % 4 == 0 ? null : "https://picsum.photos/seed/u%d/200".formatted(i));
            ps.setString(4, PROVIDERS[i % PROVIDERS.length]);
            ps.setString(5, UUID.randomUUID().toString());
            ps.setString(6, i % 5 == 0 ? "ROLE_SELLER" : "ROLE_USER");

            // 95% ACTIVE, 3% WITHDRAWN, 2% BANNED
            int r = i % 100;
            String status = r < 95 ? "ACTIVE" : (r < 98 ? "WITHDRAWN" : "BANNED");
            ps.setString(7, status);

            // 탈퇴 일시 - 과거 1년 내 랜덤
            ps.setTimestamp(8, "WITHDRAWN".equals(status)
                    ? Timestamp.from(faker.timeAndDate().past(365, TimeUnit.DAYS))
                    : null);

            // 정지 종료일 - 40% 만료(과거) / 60% 진행중(미래)
            if ("BANNED".equals(status)) {
                boolean expired = i % 5 < 2;
                Instant when = expired
                        ? faker.timeAndDate().past(30, TimeUnit.DAYS)
                        : faker.timeAndDate().future(30, TimeUnit.DAYS);
                ps.setTimestamp(9, Timestamp.from(when));
            } else {
                ps.setTimestamp(9, null);
            }

            ps.setTimestamp(10, now);
            ps.setTimestamp(11, now);
        });
    }
}
