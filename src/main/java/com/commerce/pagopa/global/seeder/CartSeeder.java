package com.commerce.pagopa.global.seeder;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Profile("local")
@Order(5)
@Component
@RequiredArgsConstructor
class CartSeeder implements Seeder {

    private final JdbcTemplate jdbc;
    private final Faker faker;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "carts";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM carts", Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        // buyer 후보 - ROLE_USER + ACTIVE
        List<Long> buyerIds = jdbc.queryForList(
                "SELECT user_id FROM users WHERE role = 'ROLE_USER' AND user_status = 'ACTIVE' ORDER BY user_id",
                Long.class
        );
        List<Long> productIds = batch.loadIds("products", "product_id");

        if (buyerIds.isEmpty() || productIds.isEmpty()) {
            throw new IllegalStateException("buyer 또는 product 부족");
        }

        int total = props.counts().carts();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        int buyerSize = buyerIds.size();
        int productSize = productIds.size();

        String sql = """
                INSERT INTO carts(quantity, user_id, product_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            ps.setInt(1, faker.number().numberBetween(1, 10));
            ps.setLong(2, buyerIds.get(i % buyerSize));
            // product를 prime stride로 분산 - user당 같은 product 중복 회피
            ps.setLong(3, productIds.get((int) ((long) i * 31 % productSize)));
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, now);
        });
    }
}
