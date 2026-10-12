package com.commerce.pagopa.media.application;

import com.commerce.pagopa.media.api.ImageUploadRequest;
import com.commerce.pagopa.media.application.response.ImageResponseDto;

public interface ImageService {
    /** 이미지 업로드 */
    ImageResponseDto upload(ImageUploadRequest uploadRequest);

    /** 이미지 삭제 */
    void delete(String imageUrl);
}
