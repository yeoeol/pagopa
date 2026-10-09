package com.commerce.pagopa.media.infrastructure.supabase;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.media.application.ImageService;
import com.commerce.pagopa.media.application.response.ImageResponseDto;
import com.commerce.pagopa.media.domain.ImageCategory;
import com.commerce.pagopa.media.infrastructure.ImageProperties;

@Primary
@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseImageService implements ImageService {

    private final ImageProperties imageProperties;
    private final SupabaseStorageProperties supabaseStorageProperties;
    private final S3Client s3Client;

    @Override
    public ImageResponseDto upload(
            MultipartFile file,
            ImageCategory category
    ) {
        validateFile(file);

        String contentType = file.getContentType();
        String objectKey = generateObjectKey(file.getOriginalFilename(), category);

        ContentStreamProvider streamProvider = ContentStreamProvider.fromInputStreamSupplier(() -> {
            try {
                return file.getInputStream();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(supabaseStorageProperties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .cacheControl("public, max-age=31536000, immutable")
                .build();

        RequestBody requestBody = RequestBody.fromContentProvider(
                streamProvider,
                file.getSize(),
                contentType
        );

        s3Client.putObject(request, requestBody);

        String imageUrl = createPublicUrl(objectKey);
        log.info(
                "[Supabase] 이미지 업로드 성공: 요청 imageUrl={}, objectKey={}, size={}bytes",
                imageUrl, objectKey, file.getSize()
        );
        return ImageResponseDto.of(imageUrl);
    }

    @Override
    public void delete(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return;
        }

        String objectKey = extractObjectKey(imageUrl);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(supabaseStorageProperties.bucket())
                .key(objectKey)
                .build();

        s3Client.deleteObject(request);
        log.info("[Supabase] 이미지 삭제 성공: 요청 imageUrl={}, objectKey={}", imageUrl, objectKey);
    }

    private String extractObjectKey(String imageUrl) {
        try {
            URI projectUri = supabaseStorageProperties.projectUrl()
                    .normalize();
            URI imageUri = URI.create(imageUrl)
                    .normalize();

            String publicPathPrefix = "/storage/v1/object/public/"
                    + supabaseStorageProperties.bucket()
                    + "/";

            String imagePath = imageUri.getPath();

            if (!hasSameOrigin(projectUri, imageUri)
                        || imagePath == null
                        || !imagePath.startsWith(publicPathPrefix)
            ) {
                throw invalidImageUrl();
            }

            String objectKey = imagePath.substring(publicPathPrefix.length());
            boolean containsUnsafePath = Arrays.stream(objectKey.split("/"))
                    .anyMatch(segment -> segment.equals(".") || segment.equals(".."));

            if (!StringUtils.hasText(objectKey) || containsUnsafePath) {
                throw invalidImageUrl();
            }

            return objectKey;
        } catch (IllegalArgumentException e) {
            throw invalidImageUrl();
        }
    }

    private boolean hasSameOrigin(URI projectUri, URI imageUri) {
        return equalsIgnoreCase(projectUri.getScheme(), imageUri.getScheme())
                && equalsIgnoreCase(projectUri.getHost(), imageUri.getHost())
                && projectUri.getPort() == imageUri.getPort()
                && imageUri.getUserInfo() == null;
    }

    private boolean equalsIgnoreCase(String expected, String actual) {
        return expected != null && expected.equalsIgnoreCase(actual);
    }

    private String createPublicUrl(String objectKey) {
        String publicPath = "/storage/v1/object/public/"
                + supabaseStorageProperties.bucket()
                + "/"
                + objectKey;
        return supabaseStorageProperties.projectUrl()
                .resolve(publicPath)
                .toString();
    }

    private BusinessException invalidImageUrl() {
        return new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Supabase 이미지 URL 형식이 올바르지 않습니다.");
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일이 비어있습니다.");
        }
        if (file.getSize() > imageProperties.maxSize()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일 크기는 5MB 이하여야 합니다.");
        }
        if (!imageProperties.allowedTypes()
                .contains(file.getContentType())
        ) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "JPG, PNG, WEBP 파일만 업로드 가능합니다.");
        }
    }

    private String generateObjectKey(String originalFilename, ImageCategory category) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        LocalDate now = LocalDate.now();
        return String.format(
                "%s/%d%02d/%s%s", category.getDirectory(), now.getYear(), now.getMonthValue(),
                UUID.randomUUID(), extension
        );
    }
}
