package com.commerce.pagopa.support.testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.testcontainers.mysql.MySQLContainer;

public final class DatabaseCleaner {

    private final DataSource dataSource;
    private final MySQLContainer mysqlContainer;

    public DatabaseCleaner(DataSource dataSource, MySQLContainer mysqlContainer) {
        this.dataSource = dataSource;
        this.mysqlContainer = mysqlContainer;
    }

    public void clean() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            verifyTestContainer(connection);

            List<String> tableNames = new ArrayList<>();

            try (
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("""
                            SELECT table_name
                            FROM information_schema.tables
                            WHERE table_schema = DATABASE()
                                AND table_type = 'BASE TABLE'
                                AND table_name <> 'flyway_schema_history'
                            """)
            ) {
                while (resultSet.next()) {
                    tableNames.add(resultSet.getString("table_name"));
                }
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute("SET FOREIGN_KEY_CHECKS = 0");

                try {
                    for (String tableName : tableNames) {
                        String escaped = tableName.replace(
                                "`",
                                "``"
                        );
                        statement.executeUpdate("TRUNCATE TABLE `" + escaped + "`");
                    }
                } finally {
                    statement.execute("SET FOREIGN_KEY_CHECKS = 1");
                }
            }
        }
    }

    private void verifyTestContainer(Connection connection) throws SQLException {
        String url = connection.getMetaData().getURL();
        String expectedEndpoint = mysqlContainer.getHost()
                + ":"
                + mysqlContainer.getMappedPort(3306)
                + "/"
                + mysqlContainer.getDatabaseName();

        if (!mysqlContainer.isRunning() || !url.contains(expectedEndpoint)) {
            throw new IllegalStateException(
                    "테스트 컨테이너가 아닌 DB는 정리할 수 없습니다: " + url
            );
        }
    }
}
