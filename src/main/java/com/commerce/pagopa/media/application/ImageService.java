package com.commerce.pagopa.media.application;

import com.commerce.pagopa.media.application.response.ImageResponseDto;
import com.commerce.pagopa.media.domain.ImageCategory;
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
