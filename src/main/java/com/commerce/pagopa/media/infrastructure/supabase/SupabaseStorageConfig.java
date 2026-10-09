package com.commerce.pagopa.media.infrastructure.supabase;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import com.commerce.pagopa.media.infrastructure.ImageProperties;

@Configuration
@EnableConfigurationProperties(
    {
            ImageProperties.class,
            SupabaseStorageProperties.class
    }
)
public class SupabaseStorageConfig {

    @Bean(destroyMethod = "close")
    public S3Client supabaseS3Client(
            SupabaseStorageProperties supabaseStorageProperties
    ) {
        return S3Client.builder()
                .endpointOverride(supabaseStorageProperties.endpoint())
                .region(Region.of(supabaseStorageProperties.region()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        supabaseStorageProperties.accessKey(),
                                        supabaseStorageProperties.secretKey()
                                )
                        )
                )
                .forcePathStyle(true)
                .overrideConfiguration(
                        configuration -> configuration
                                .apiCallAttemptTimeout(Duration.ofSeconds(20))
                                .apiCallTimeout(Duration.ofSeconds(60))
                                .retryStrategy(RetryMode.STANDARD)
                )
                .build();
    }
}
