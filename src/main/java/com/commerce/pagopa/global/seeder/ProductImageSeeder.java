package com.commerce.pagopa.global.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("local")
@Order(4)
@Component
@RequiredArgsConstructor
class ProductImageSeeder implements Seeder {

    // 상품당 이미지 개수 - 첫 번째 이미지(order=0)가 thumbnail
    private static final int IMAGES_PER_PRODUCT = 3;

    private final JdbcTemplate jdbc;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "product_images";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM product_images", Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        List<Long> productIds = batch.loadIds("products", "product_id");
        if (productIds.isEmpty()) {
            throw new IllegalStateException("product 없음 - products 시드 먼저 필요");
        }

        // 총량을 상품 수에 종속시켜 partial 라운드 자체를 제거
        int productSize = productIds.size();
        int total = productSize * IMAGES_PER_PRODUCT;

        String sql = """
                INSERT INTO product_images(image_url, display_order, is_thumbnail, product_id)
                VALUES (?, ?, ?, ?)
                """;

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            // 0..total-1을 (productIdx, order)로 분해 - 모든 상품이 정확히 IMAGES_PER_PRODUCT장
            int productIdx = i / IMAGES_PER_PRODUCT;
            int order = i % IMAGES_PER_PRODUCT;
            ps.setString(1, "https://picsum.photos/seed/p%d_%d/600/600".formatted(productIdx, order));
            ps.setInt(2, order);
            ps.setBoolean(3, order == 0);
            ps.setLong(4, productIds.get(productIdx));
        });
    }
}
