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

    // user별 시작 product offset - product 풀에서 user끼리 겹치지 않게 시작점 분산
    private static final int USER_START_OFFSET_STRIDE = 31;

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

        int scrapsToInsert = props.counts().scraps();
        int buyerCount = buyerIds.size();
        int productCount = productIds.size();

        // user 한 명당 부여할 scrap 개수 - 한 user 안에서 product 중복 회피 위해 보폭 1로 순회
        int scrapsPerUser = (scrapsToInsert + buyerCount - 1) / buyerCount;
        if (scrapsPerUser > productCount) {
            throw new IllegalStateException(
                    "user당 scrap(%d)이 product 수(%d) 초과 - unique 충족 불가"
                            .formatted(scrapsPerUser, productCount)
            );
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        String sql = """
                INSERT INTO scraps(target_id, target_type, user_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        batch.batchInsert(sql, scrapsToInsert, props.batchSize(), (ps, scrapIdx) -> {
            int userIdx = scrapIdx / scrapsPerUser;
            int positionInUser = scrapIdx % scrapsPerUser;

            // 분산 패턴 E - user별 시작점은 31씩 떨어뜨리고, 같은 user 안에서는 +1씩 보폭 1로 진행
            //   같은 user 내: positionInUser가 0,1,2,...,scrapsPerUser-1 → 슬롯이 1씩 증가 → 충돌 0
            //   다른 user 간: user_id가 달라 unique 제약과 무관
            int userStartOffset = (int) (((long) userIdx * USER_START_OFFSET_STRIDE) % productCount);
            int productIdx = (userStartOffset + positionInUser) % productCount;

            ps.setLong(1, productIds.get(productIdx));
            ps.setString(2, "PRODUCT");
            ps.setLong(3, buyerIds.get(userIdx));
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, now);
        });
    }
}
