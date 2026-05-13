package com.commerce.pagopa.global.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Profile("local")
@Order(9)
@Component
@RequiredArgsConstructor
class ScrapSeeder implements Seeder {

    private final JdbcTemplate jdbc;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "scraps";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM scraps", Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        List<Long> buyerIds = jdbc.queryForList(
                "SELECT user_id FROM users WHERE role = 'ROLE_USER' AND user_status = 'ACTIVE' ORDER BY user_id",
                Long.class);
        List<Long> productIds = batch.loadIds("products", "product_id");

        if (buyerIds.isEmpty() || productIds.isEmpty()) {
            throw new IllegalStateException("buyer/product 부족");
        }

        int total = props.counts().scraps();
        int buyerSize = buyerIds.size();
        int productSize = productIds.size();

        // user당 슬롯 수 - unique(user_id, target_type, target_id) 충족 위해 동일 user 안에서 product 중복 회피
        int slotsPerUser = (total + buyerSize - 1) / buyerSize;
        if (slotsPerUser > productSize) {
            throw new IllegalStateException(
                    "user당 slot(%d)이 product 수(%d) 초과 - 중복 회피 불가"
                            .formatted(slotsPerUser, productSize)
            );
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        String sql = """
                INSERT INTO scraps(target_id, target_type, user_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            int userIdx = i / slotsPerUser;
            int slotInUser = i % slotsPerUser;
            // stride 31로 같은 user 내 product 충돌 회피 (slotsPerUser * 31 < productSize 전제)
            int productIdx = (int) (((long) slotInUser * 31 + userIdx) % productSize);

            ps.setLong(1, productIds.get(productIdx));
            ps.setString(2, "PRODUCT");
            ps.setLong(3, buyerIds.get(userIdx));
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, now);
        });
    }
}
