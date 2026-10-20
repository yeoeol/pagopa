package com.commerce.pagopa.media.infrastructure.azure;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(
        properties = {
                "app.storage.provider.type=azure",
                "spring.cloud.azure.storage.blob.enabled=true"
        }
)
@ActiveProfiles("test")
public class AzureImageServiceIntegrationTest {

    private static final DockerImageName AZURITE_IMAGE = DockerImageName
            .parse("mcr.microsoft.com/azure-storage/azurite:3.35.0");
    private static final int AZURITE_BLOB_PORT = 10000;

    @Container
    static final GenericContainer<?> AZURITE = new GenericContainer<>(AZURITE_IMAGE)
            .withExposedPorts(AZURITE_BLOB_PORT)
            .withCommand(
                    "azurite-blob",
                    "--blobHost",
                    "0.0.0.0",
                    "--skipApiVersionCheck"
            );

    @DynamicPropertySource
    static void azureProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.cloud.azure.storage.blob.endpoint",
                () -> "http://" + AZURITE.getHost()
                        + ":" + AZURITE.getMappedPort(AZURITE_BLOB_PORT)
                        + "/devstoreaccount1"
        );

        registry.add(
                "app.storage.provider.azure.base-url",
                () -> "http://" + AZURITE.getHost()
                        + ":" + AZURITE.getMappedPort(AZURITE_BLOB_PORT)
                        + "/devstoreaccount1/test-container"
        );
    }
}
