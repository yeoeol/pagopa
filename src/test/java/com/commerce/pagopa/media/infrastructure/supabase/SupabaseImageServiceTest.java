package com.commerce.pagopa.media.infrastructure.supabase;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import com.commerce.pagopa.media.infrastructure.ImageProperties;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SupabaseImageServiceTest {

    private static final String BUCKET = "pagopa-images";
    private static final URI PROJECT_URL = URI.create("https://project-ref.supabase.co");

    @Mock
    private S3Client s3Client;

    private SupabaseImageService supabaseImageService;

    @BeforeEach
    void setUp() {
        ImageProperties imageProperties = new ImageProperties(
                5_242_880,
                List.of(
                        "image/jpeg",
                        "image/png",
                        "image/webp"
                )
        );
        SupabaseStorageProperties storageProperties = new SupabaseStorageProperties(
                URI.create("https://project-ref.storage.supabase.co/storage/v1/s3"),
                "ap-northeast-2",
                "test-access-key",
                "test-secret-key",
                BUCKET,
                PROJECT_URL
        );
        supabaseImageService = new SupabaseImageService(
                imageProperties,
                storageProperties,
                s3Client
        );
    }

    @Test
    void delete_extractsObjectKeyFromPublicUrl() {
        String imageUrl = PROJECT_URL
                + "/storage/v1/object/public/"
                + BUCKET
                + "/product/202610/550e8400-e29b-41d4-a716-446655440000.jpg";

        supabaseImageService.delete(imageUrl);

        ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(requestCaptor.capture());
        DeleteObjectRequest request = requestCaptor.getValue();

        assertThat(request.bucket()).isEqualTo(BUCKET);
        assertThat(request.key()).isEqualTo("product/202610/550e8400-e29b-41d4-a716-446655440000.jpg");
    }

    @Test
    void delete_ignoresUrlFromDifferentOrigin() {
        String imageUrl = "https://attacker.example/storage/v1/object/public/"
                + BUCKET
                + "/product/202610/image.jpg";

        assertThatCode(
                () -> supabaseImageService.delete(imageUrl)
        ).doesNotThrowAnyException();

        verifyNoInteractions(s3Client);
    }

    @Test
    void delete_ignoresRelativeUrl() {
        assertThatCode(
                () -> supabaseImageService.delete("product/202610/image.jpg")
        ).doesNotThrowAnyException();

        verifyNoInteractions(s3Client);
    }
}
