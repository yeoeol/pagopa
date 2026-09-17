package com.commerce.pagopa.support.testcontainers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    private static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:8.0.36");
    private static final DockerImageName AZURITE_IMAGE = DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:3.35.0");
    private static final int AZURITE_BLOB_PORT = 10000;

    @Bean
    @ServiceConnection
    MySQLContainer mysqlContainer() {
        return new MySQLContainer(MYSQL_IMAGE);
    }

    @Bean
    GenericContainer<?> azuriteContainer() {
        return new GenericContainer<>(AZURITE_IMAGE)
                .withExposedPorts(AZURITE_BLOB_PORT)
                .withCommand("azurite-blob", "--blobHost", "0.0.0.0", "--skipApiVersionCheck");
    }

    @Bean
    DynamicPropertyRegistrar azuriteProperties(@Qualifier("azuriteContainer") GenericContainer<?> azuriteContainer) {
        return registry -> {
            String endpoint = "http://" + azuriteContainer.getHost() + ":"
                    + azuriteContainer.getMappedPort(AZURITE_BLOB_PORT) + "/devstoreaccount1";
            registry.add("spring.cloud.azure.storage.blob.endpoint", () -> endpoint);
            registry.add("app.azure.base-url", () -> endpoint + "/test-container");
        };
    }
}
