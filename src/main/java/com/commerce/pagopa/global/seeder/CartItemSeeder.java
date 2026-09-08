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
@Order(11)
@Component
@RequiredArgsConstructor
class CartItemSeeder implements Seeder {

    private final JdbcTemplate jdbc;
    private final Faker faker;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "cart_item";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + name(), Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        List<Long> cartIds = batch.loadIds("cart", "cart_id");
        List<Long> productIds = batch.loadIds("product", "product_id");

        if (cartIds.isEmpty() || productIds.isEmpty()) {
            throw new IllegalStateException("cart 또는 product 부족");
        }

        int total = props.counts().cartItems();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        int cartSize = cartIds.size();
        int productSize = productIds.size();

        if ((long) total > (long) cartSize * productSize) {
            throw new IllegalArgumentException("cartItems exceeds unique cart-product pairs");
        }

        String sql = """
                INSERT INTO cart_item(cart_id, product_id, cart_quantity, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        batch.batchInsert(sql, total, props.batchSize(), (ps, i) -> {
            ps.setLong(1, cartIds.get(i % cartSize));
            ps.setLong(2, productIds.get((i / cartSize) % productSize));
            ps.setInt(3, faker.number().numberBetween(1, 10));
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, now);
        });
    }
}
