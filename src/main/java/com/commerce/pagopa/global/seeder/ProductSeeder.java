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
@Order(3)
@Component
@RequiredArgsConstructor
class ProductSeeder implements Seeder {

    private final JdbcTemplate jdbc;
    private final Faker faker;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "products";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + name(), Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        // seller 후보 - ROLE_SELLER + ACTIVE 사용자만
        List<Long> sellerIds = jdbc.queryForList(
                """
                SELECT u.user_id
                FROM seller s
                WHERE status = 'ACTIVE'
                ORDER BY s.seller_id
                """,
                Long.class
        );

        List<Long> leafCategoryIds = jdbc.queryForList(
                """
                SELECT c.category_id
                FROM category c
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM category child
                    WHERE child.parent_id = c.category_id
                );
                """,
                Long.class
        );

        if (sellerIds.isEmpty() || leafCategoryIds.isEmpty()) {
            throw new IllegalStateException("seller 또는 leaf category 부족 - user/category 시드 먼저 필요");
        }

        int total = props.counts().products();
        String sql = """
                INSERT INTO product(
                    name,
                    description,
                    price,
                    stock_quantity,
                    status,
                    category_id,
                    seller_id,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        int sellerSize = sellerIds.size();
        int categorySize = leafCategoryIds.size();

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            int price = faker.number().numberBetween(1_000, 200_000);

            // 재고 - 5% 품절(stockQuantity=0), 나머지 1~500
            boolean soldOut = i % 20 == 0;
            int stockQuantity = soldOut ? 0 : faker.number().numberBetween(1, 500);

            // 상태 - stockQuantity=0이면 SOLD_OUT 고정, 그 외 ACTIVE/INACTIVE/HIDDEN 분배
            String status;
            if (soldOut) {
                status = "SOLD_OUT";
            } else {
                int r = i % 17;
                status = r < 14 ? "ACTIVE" : (r < 16 ? "INACTIVE" : "HIDDEN");
            }

            ps.setString(1, "%s-%d".formatted(faker.commerce().productName(), i));
            ps.setString(2, faker.lorem().sentence(20));
            ps.setInt(3, price);
            ps.setInt(4, stockQuantity);
            ps.setString(5, status);
            ps.setLong(6, leafCategoryIds.get(i % categorySize));
            ps.setLong(7, sellerIds.get(i % sellerSize));
            ps.setTimestamp(8, now);
            ps.setTimestamp(9, now);
        });
    }
}
