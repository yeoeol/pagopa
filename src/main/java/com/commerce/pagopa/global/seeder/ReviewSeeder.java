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
        return "reviews";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM reviews", Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        // OrderProduct와 1:1 매핑 - @OneToOne 제약 충족
        List<Long> orderProductIds = batch.loadIds("order_product", "order_product_id");
        List<Long> buyerIds = jdbc.queryForList(
                "SELECT user_id FROM users WHERE role = 'ROLE_USER' AND user_status = 'ACTIVE' ORDER BY user_id",
                Long.class);

        if (orderProductIds.isEmpty() || buyerIds.isEmpty()) {
            throw new IllegalStateException("order_product/buyer 부족");
        }

        int total = props.counts().reviews();
        if (total > orderProductIds.size()) {
            throw new IllegalStateException(
                    "reviews(%d) > order_product(%d) - 1:1 매핑 불가".formatted(total, orderProductIds.size())
            );
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        int buyerSize = buyerIds.size();

        String sql = """
                INSERT INTO reviews(rating, content, user_id, order_product_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            ps.setInt(1, i % 5 + 1);                          // 1~5 균등
            ps.setString(2, faker.lorem().sentence(15));
            ps.setLong(3, buyerIds.get(i % buyerSize));
            ps.setLong(4, orderProductIds.get(i));            // 처음 total개 OrderProduct에 부여
            ps.setTimestamp(5, now);
            ps.setTimestamp(6, now);
        });
    }
}
