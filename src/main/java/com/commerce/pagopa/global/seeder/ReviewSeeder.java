package com.commerce.pagopa.global.seeder;

import net.datafaker.Faker;

import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Profile("local")
@Order(7)
@Component
@RequiredArgsConstructor
class ReviewSeeder implements Seeder {

    private final JdbcTemplate jdbc;
    private final Faker faker;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "review";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + name(), Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        // OrderProduct와 1:1 매핑 - @OneToOne 제약 충족
        List<Long> orderItemIds = batch.loadIds("order_item", "order_item_id");

        if (orderItemIds.isEmpty()) {
            throw new IllegalStateException("order_item 부족");
        }

        int total = props.counts().reviews();
        if (total > orderItemIds.size()) {
            throw new IllegalStateException(
                    "review(%d) > order_item(%d) - 1:1 매핑 불가".formatted(total, orderItemIds.size())
            );
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        String sql = """
                INSERT INTO review(content, rating, order_item_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            ps.setString(1, faker.lorem().sentence(15));
            ps.setInt(2, i % 5 + 1);                          // 1~5 균등
            ps.setLong(3, orderItemIds.get(i));            // 처음 total개 OrderItem에 부여
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, now);
        });
    }
}
