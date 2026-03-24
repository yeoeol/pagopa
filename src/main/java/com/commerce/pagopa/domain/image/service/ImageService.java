package com.commerce.pagopa.domain.image.service;

import com.commerce.pagopa.domain.image.ImageCategory;
import com.commerce.pagopa.domain.image.dto.response.ImageResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    ImageResponseDto upload(MultipartFile file, ImageCategory category);
    void delete(String imageUrl);
}
