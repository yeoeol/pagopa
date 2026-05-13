package com.commerce.pagopa.global.seeder;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

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
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        // seller 후보 - ROLE_SELLER + ACTIVE 사용자만
        List<Long> sellerIds = jdbc.queryForList(
                "SELECT user_id FROM users WHERE role = 'ROLE_SELLER' AND user_status = 'ACTIVE' ORDER BY user_id",
                Long.class
        );
        // leaf category - depth=2만
        List<Long> leafCategoryIds = jdbc.queryForList(
                "SELECT category_id FROM categories WHERE depth = 2 ORDER BY category_id",
                Long.class
        );

        if (sellerIds.isEmpty() || leafCategoryIds.isEmpty()) {
            throw new IllegalStateException("seller 또는 leaf category 부족 - users/categories 시드 먼저 필요");
        }

        int total = props.counts().products();
        String sql = """
                INSERT INTO products(
                    name, description, price, discount_price, stock,
                    status, category_id, user_id, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        int sellerSize = sellerIds.size();
        int categorySize = leafCategoryIds.size();

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            int price = faker.number().numberBetween(1_000, 200_000);
            BigDecimal priceVal = BigDecimal.valueOf(price);

            // 할인가 - 30% 확률 부여, 원래가의 85% 적용 후 100원 단위 내림
            boolean discounted = i % 10 < 3;
            BigDecimal discountVal = null;
            if (discounted) {
                long rounded = (long) (price * 0.85) / 100 * 100;
                discountVal = BigDecimal.valueOf(rounded);
            }

            // 재고 - 5% 품절(stock=0), 나머지 1~500
            boolean soldOut = i % 20 == 0;
            int stock = soldOut ? 0 : faker.number().numberBetween(1, 500);

            // 상태 - stock=0이면 SOLDOUT 고정, 그 외 ACTIVE/INACTIVE/HIDDEN 분배
            String status;
            if (soldOut) {
                status = "SOLDOUT";
            } else {
                int r = i % 17;
                status = r < 14 ? "ACTIVE" : (r < 16 ? "INACTIVE" : "HIDDEN");
            }

            ps.setString(1, "%s-%d".formatted(faker.commerce().productName(), i));
            ps.setString(2, faker.lorem().sentence(20));
            ps.setBigDecimal(3, priceVal);
            ps.setBigDecimal(4, discountVal);
            ps.setInt(5, stock);
            ps.setString(6, status);
            ps.setLong(7, leafCategoryIds.get(i % categorySize));
            ps.setLong(8, sellerIds.get(i % sellerSize));
            ps.setTimestamp(9, now);
            ps.setTimestamp(10, now);
        });
    }
}
