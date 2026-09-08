package com.commerce.pagopa.global.seeder;

import net.datafaker.Faker;

import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import lombok.RequiredArgsConstructor;

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
        return "category";
    }

    @Override
    public boolean shouldRun() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM category", Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        // depth 0 - 루트 카테고리
        batch.batchInsert(
                "INSERT INTO category(name, parent_id) VALUES(?, NULL)",
                ROOT_COUNT, props.batchSize(),
                (ps, i) -> ps.setString(1, faker.commerce().department() + "-" + i)
        );
        List<Long> rootIds = jdbc.queryForList(
                "SELECT category_id FROM category WHERE parent_id IS NULL ORDER BY category_id", Long.class);

        // depth 1 - 중분류 (루트당 MID_PER_ROOT)
        int midTotal = ROOT_COUNT * MID_PER_ROOT;
        batch.batchInsert(
                "INSERT INTO category(name, parent_id) VALUES(?, ?)",
                midTotal, props.batchSize(),
                (ps, i) -> {
                    ps.setString(1, faker.commerce().material() + "-" + i);
                    ps.setLong(2, rootIds.get(i / MID_PER_ROOT));
                }
        );
        List<Long> midIds = jdbc.queryForList(
                """
                WITH RECURSIVE descendants AS (
                    SELECT category_id, parent_id, name, created_at, updated_at, 0 AS depth
                    FROM category
                    WHERE parent_id IS NULL
                
                    UNION ALL
                
                    SELECT c.category_id, c.parent_id, c.name, c.created_at, c.updated_at, d.depth+1
                    FROM category c
                    JOIN descendants d
                         ON c.parent_id = d.category_id
                    WHERE d.depth < 1
                )
                SELECT category_id
                FROM descendants
                WHERE depth = 1
                ORDER BY category_id
                """,
                Long.class
        );

        // depth 2 - 소분류 (중분류당 LEAF_PER_MID)
        int leafTotal = midIds.size() * LEAF_PER_MID;
        batch.batchInsert(
                "INSERT INTO category(name, parent_id) VALUES(?, ?)",
                leafTotal, props.batchSize(),
                (ps, i) -> {
                    ps.setString(1, faker.commerce().productName() + "-" + i);
                    ps.setLong(2, midIds.get(i / LEAF_PER_MID));
                }
        );
    }
}
