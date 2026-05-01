package com.commerce.pagopa.image.application;

import com.commerce.pagopa.image.application.dto.response.ImageResponseDto;
import com.commerce.pagopa.image.domain.model.ImageCategory;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    /**
     * 이미지 업로드
     */
    ImageResponseDto upload(MultipartFile file, ImageCategory category);
    /**
     * 이미지 삭제
     */
    void delete(String imageUrl);
}
