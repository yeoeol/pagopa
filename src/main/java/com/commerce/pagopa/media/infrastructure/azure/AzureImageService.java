package com.commerce.pagopa.media.infrastructure.azure;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.media.api.ImageUploadRequest;
import com.commerce.pagopa.media.application.ImageService;
import com.commerce.pagopa.media.application.response.ImageResponseDto;
import com.commerce.pagopa.media.domain.ImageCategory;
import com.commerce.pagopa.media.infrastructure.ImageProperties;

@ConditionalOnProperty(
        name = "app.storage.provider.type",
        havingValue = "azure"
)
@Slf4j
@Service
@RequiredArgsConstructor
public class AzureImageService implements ImageService {

    private final ImageProperties imageProperties;
    private final AzureStorageProperties azureStorageProperties;
    private final BlobContainerClient blobContainerClient;

    @Override
    public ImageResponseDto upload(ImageUploadRequest uploadRequest) {
        validateFile(uploadRequest);

        String blobName = generateBlobName(
                uploadRequest.originalFilename(),
                uploadRequest.imageCategory()
        );
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(uploadRequest.contentType());

            blobClient.upload(
                    uploadRequest.content()
                            .openStream(),
                    uploadRequest.size(),
                    true
            );
            blobClient.setHttpHeaders(headers);

            String imageUrl = azureStorageProperties.baseUrl() + "/" + blobName;
            log.info(
                    "[Azure] 이미지 업로드 성공: blobName={}, size={}bytes",
                    blobName,
                    uploadRequest.size()
            );

            return ImageResponseDto.of(imageUrl);

        } catch (IOException e) {
            log.error(
                    "[Azure] 이미지 업로드 실패: {}",
                    e.getMessage()
            );
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String imageUrl) {
        if (!StringUtils.hasText(imageUrl))
            return;

        String blobName = extractBlobName(imageUrl);
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            if (blobClient.exists()) {
                blobClient.delete();
                log.info(
                        "[Azure] 이미지 삭제 성공: blobName={}",
                        blobName
                );
            }
        } catch (Exception e) {
            log.warn(
                    "[Azure] 이미지 삭제 실패: blobName={}, error={}",
                    blobName,
                    e.getMessage()
            );
        }
    }

    private void validateFile(ImageUploadRequest uploadRequest) {
        if (uploadRequest == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일이 비어있습니다."
            );
        }
        if (uploadRequest.size() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일의 크기가 0보다 커야 합니다."
            );
        }
        if (uploadRequest.size() > imageProperties.maxSize()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일 크기는 5MB 이하여야 합니다."
            );
        }
        if (!imageProperties.allowedTypes()
                .contains(uploadRequest.contentType())) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "JPG, PNG, WEBP 파일만 업로드 가능합니다."
            );
        }
    }

    private String extractBlobName(String imageUrl) {
        String containerName = blobContainerClient.getBlobContainerName();
        int containerIdx = imageUrl.indexOf(containerName);
        return imageUrl.substring(containerIdx + containerName.length() + 1);
    }

    private String generateBlobName(String originalFilename, ImageCategory category) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        LocalDate now = LocalDate.now();
        return String.format(
                "%s/%d%02d/%s%s",
                category.getDirectory(),
                now.getYear(),
                now.getMonthValue(),
                UUID.randomUUID(),
                extension
        );
    }
}
