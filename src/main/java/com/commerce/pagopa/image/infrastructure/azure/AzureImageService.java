package com.commerce.pagopa.image.infrastructure.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.image.application.ImageService;
import com.commerce.pagopa.image.application.dto.response.ImageResponseDto;
import com.commerce.pagopa.image.domain.model.ImageCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AzureImageService implements ImageService {

    @Value("${app.azure.base-url}")
    private String baseUrl;

    @Value("${app.image.max-size}")
    private long maxSize;

    @Value("${app.image.allowed-types}")
    private List<String> allowedTypes;

    private final BlobContainerClient blobContainerClient;

    public AzureImageService(
            BlobServiceClient blobServiceClient,
            @Value("${app.azure.container-name}") String containerName
    ) {
        this.blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);;
        if (!this.blobContainerClient.exists()) {
            this.blobContainerClient.create();
        }
    }

    @Override
    public ImageResponseDto upload(MultipartFile file, ImageCategory category) {
        validateFile(file);

        String blobName = generateBlobName(file.getOriginalFilename(), category);
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType());

            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);

            String imageUrl = baseUrl + "/" + blobName;
            log.info("[Azure] 이미지 업로드 성공: blobName={}, size={}bytes", blobName, file.getSize());

            return ImageResponseDto.of(imageUrl);

        } catch (IOException e) {
            log.error("[Azure] 이미지 업로드 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) return;

        String blobName = extractBlobName(imageUrl);
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            if (blobClient.exists()) {
                blobClient.delete();
                log.info("[Azure] 이미지 삭제 성공: blobName={}", blobName);
            }
        } catch (Exception e) {
            log.warn("[Azure] 이미지 삭제 실패: blobName={}, error={}", blobName, e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일이 비어있습니다.");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일 크기는 5MB 이하여야 합니다.");
        }
        if (!allowedTypes.contains(file.getContentType())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "JPG, PNG, WEBP 파일만 업로드 가능합니다.");
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
        return String.format("%s/%d%02d/%s%s",
                category.getDirectory(),
                now.getYear(),
                now.getMonthValue(),
                UUID.randomUUID(),
                extension
        );
    }
}
