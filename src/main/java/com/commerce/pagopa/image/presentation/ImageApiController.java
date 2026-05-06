package com.commerce.pagopa.image.presentation;

import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.image.application.ImageService;
import com.commerce.pagopa.image.application.dto.response.ImageResponseDto;
import com.commerce.pagopa.image.domain.model.ImageCategory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "IMAGE API", description = "이미지 관리 API")
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageApiController {

    private final ImageService imageService;

    @Operation(summary = "이미지 업로드", description = "특정 이미지 카테고리에 이미지를 업로드합니다.")
    @PostMapping("/upload/{category}")
    public ResponseEntity<ApiResponse<ImageResponseDto>> upload(
            @PathVariable("category") ImageCategory category,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(imageService.upload(file, category))
        );
    }

    @Operation(summary = "이미지 삭제", description = "이미지 url을 기반으로 클라우드 저장소에서 이미지를 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestParam("imageUrl") String imageUrl
    ) {
        imageService.delete(imageUrl);
        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }
}
