package com.commerce.pagopa.support.testcontainers;

import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class DatabaseCleanupConfig {

    @Bean
    DatabaseCleaner databaseCleaner(
            DataSource dataSource,
            MySQLContainer mysqlContainer
    ) {
        return new DatabaseCleaner(
                dataSource,
                mysqlContainer
        );
    }
}
