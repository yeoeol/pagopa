package com.commerce.pagopa.global.seeder;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("local")
@Order(1)
@Component
@RequiredArgsConstructor
class CategorySeeder implements Seeder {

    private static final int ROOT_COUNT = 5;
    private static final int MID_PER_ROOT = 4;   // depth 1 = 20
    private static final int LEAF_PER_MID = 5;   // depth 2 = 100

    private final JdbcTemplate jdbc;
    private final Faker faker;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "categories";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM categories", Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        // depth 0 - 루트 카테고리
        batch.batchInsert(
                "INSERT INTO categories(name, depth, parent_id) VALUES(?, 0, NULL)",
                ROOT_COUNT, props.batchSize(),
                (ps, i) -> ps.setString(1, faker.commerce().department() + "-" + i)
        );
        List<Long> rootIds = jdbc.queryForList(
                "SELECT category_id FROM categories WHERE depth = 0 ORDER BY category_id", Long.class);

        // depth 1 - 중분류 (루트당 MID_PER_ROOT)
        int midTotal = ROOT_COUNT * MID_PER_ROOT;
        batch.batchInsert(
                "INSERT INTO categories(name, depth, parent_id) VALUES(?, 1, ?)",
                midTotal, props.batchSize(),
                (ps, i) -> {
                    ps.setString(1, faker.commerce().material() + "-" + i);
                    ps.setLong(2, rootIds.get(i / MID_PER_ROOT));
                }
        );
        List<Long> midIds = jdbc.queryForList(
                "SELECT category_id FROM categories WHERE depth = 1 ORDER BY category_id", Long.class);

        // depth 2 - 소분류 (중분류당 LEAF_PER_MID)
        int leafTotal = midIds.size() * LEAF_PER_MID;
        batch.batchInsert(
                "INSERT INTO categories(name, depth, parent_id) VALUES(?, 2, ?)",
                leafTotal, props.batchSize(),
                (ps, i) -> {
                    ps.setString(1, faker.commerce().productName() + "-" + i);
                    ps.setLong(2, midIds.get(i / LEAF_PER_MID));
                }
        );
    }
}
