package com.commerce.pagopa.media.infrastructure.azure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;

import com.commerce.pagopa.media.infrastructure.ImageProperties;

@Configuration
@EnableConfigurationProperties(
    {
            ImageProperties.class,
            AzureStorageProperties.class
    }
)
public class AzureStorageConfig {

    @Bean
    public BlobContainerClient blobContainerClient(
            AzureStorageProperties azureStorageProperties,
            BlobServiceClient blobServiceClient
    ) {
        BlobContainerClient blobContainerClient = blobServiceClient
                .getBlobContainerClient(azureStorageProperties.containerName());

        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
        }

        return blobContainerClient;
    }
}
