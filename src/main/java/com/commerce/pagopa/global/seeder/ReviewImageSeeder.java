package com.commerce.pagopa.global.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("local")
@Order(8)
@Component
@RequiredArgsConstructor
class ReviewImageSeeder implements Seeder {

    private final JdbcTemplate jdbc;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "review_images";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM review_images", Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        List<Long> reviewIds = batch.loadIds("reviews", "review_id");
        if (reviewIds.isEmpty()) {
            throw new IllegalStateException("review 없음");
        }

        int total = props.counts().reviewImages();
        if (total > reviewIds.size()) {
            throw new IllegalStateException(
                    "review_images(%d) > reviews(%d) - 처음 N개 Review에 1장씩 부여 전제"
                            .formatted(total, reviewIds.size())
            );
        }

        String sql = """
                INSERT INTO review_images(image_url, display_order, review_id)
                VALUES (?, ?, ?)
                """;

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            ps.setString(1, "https://picsum.photos/seed/r%d/400/400".formatted(i));
            ps.setInt(2, 0);
            ps.setLong(3, reviewIds.get(i));
        });
    }
}
