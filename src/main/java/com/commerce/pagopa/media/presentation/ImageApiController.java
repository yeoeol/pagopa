package com.commerce.pagopa.media.presentation;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.media.api.ImageUploadRequest;
import com.commerce.pagopa.media.application.ImageService;
import com.commerce.pagopa.media.application.response.ImageResponseDto;
import com.commerce.pagopa.media.domain.ImageCategory;

@Tag(
        name = "IMAGE API",
        description = "이미지 관리 API"
)
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageApiController {

    private final ImageService imageService;

    @Operation(
            summary = "이미지 업로드",
            description = "특정 이미지 카테고리에 이미지를 업로드합니다."
    )
    @PostMapping(
            value = "/upload/{category}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ImageResponseDto>> upload(
            @PathVariable("category") ImageCategory category,
            @Parameter(
                    description = "업로드할 이미지 파일 (jpg, png 등)",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE
                    )
            )
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        imageService.upload(
                                new ImageUploadRequest(
                                        category,
                                        file.getOriginalFilename(),
                                        file.getContentType(),
                                        file.getSize(),
                                        file::getInputStream
                                )
                        )
                )
        );
    }
}
