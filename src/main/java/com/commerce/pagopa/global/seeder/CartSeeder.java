package com.commerce.pagopa.global.seeder;

import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Profile("local")
@Order(5)
@Component
@RequiredArgsConstructor
class CartSeeder implements Seeder {

    private final JdbcTemplate jdbc;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "cart";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + name(), Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        // buyer 후보 - ROLE_USER + ACTIVE
        List<Long> buyerIds = jdbc.queryForList(
                """
                SELECT user_id
                FROM user u
                JOIN user_role ur
                    ON u.user_id = ur.user_id
                JOIN role r
                    ON ur.role_id = r.role_id
                WHERE u.status = 'ACTIVE'
                    AND r.code = 'ROLE_USER'
                ORDER BY user_id
                """,
                Long.class
        );
        if (buyerIds.isEmpty()) {
            throw new IllegalStateException("buyer 부족");
        }

        int total = props.counts().carts();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        int buyerSize = buyerIds.size();

        String sql = """
                INSERT INTO cart(user_id, created_at, updated_at)
                VALUES (?, ?, ?)
                """;

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            ps.setLong(1, buyerIds.get(i % buyerSize));
            ps.setTimestamp(2, now);
            ps.setTimestamp(3, now);
        });
    }
}
