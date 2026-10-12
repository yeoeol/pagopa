package com.commerce.pagopa.media.presenatation;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.media.application.ImageService;

@Profile("local")
@Tag(
        name = "IMAGE API",
        description = "이미지 관리 API"
)
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageLocalApiController {

    private final ImageService imageService;

    @Operation(
            summary = "이미지 삭제",
            description = "이미지 url을 기반으로 클라우드 저장소에서 이미지를 삭제합니다."
    )
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(
                    description = "삭제할 이미지의 전체 URL",
                    example = "https://yeoeol.blob.core.windows.net/pagopa/product/202605/f31dc2d8-2c23.jpg"
            )
            @RequestParam("imageUrl") String imageUrl
    ) {
        imageService.delete(imageUrl);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
