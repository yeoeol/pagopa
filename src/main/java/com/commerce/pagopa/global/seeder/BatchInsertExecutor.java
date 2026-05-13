package com.commerce.pagopa.global.seeder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class BatchInsertExecutor {

    @FunctionalInterface
    public interface RowBinder {
        void bind(PreparedStatement ps, int globalIndex) throws SQLException;
    }

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;

    public BatchInsertExecutor(JdbcTemplate jdbc, PlatformTransactionManager txm) {
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(txm);
    }

    /**
     * totalRows건을 batchSize 단위로 잘라 chunk마다 별도 트랜잭션으로 INSERT
     * binder.bind(ps, globalIndex) - globalIndex는 0..totalRows-1 전역 인덱스
     */
    public void batchInsert(String sql, int totalRows, int batchSize, RowBinder binder) {
        int chunks = (totalRows + batchSize - 1) / batchSize;
        for (int c = 0; c < chunks; c++) {
            int from = c * batchSize;
            int to = Math.min(from + batchSize, totalRows);
            int size = to - from;
            int chunkStart = from;

            tx.executeWithoutResult(_ -> jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    binder.bind(ps, chunkStart + i);
                }
                @Override
                public int getBatchSize() {
                    return size;
                }
            }));

            if ((c + 1) % 10 == 0 || c + 1 == chunks) {
                log.info("  inserted {} / {}", to, totalRows);
            }
        }
    }

    /** ID 컬럼 전체를 오름차순으로 로딩 - 시드 후 FK 참조용 ID 풀 확보 */
    public List<Long> loadIds(String table, String idColumn) {
        return jdbc.queryForList(
                "SELECT %s FROM %s ORDER BY %s".formatted(idColumn, table, idColumn),
                Long.class
        );
    }
}
