package com.commerce.pagopa.domain.image.controller;

import com.commerce.pagopa.domain.image.ImageCategory;
import com.commerce.pagopa.domain.image.dto.response.ImageResponseDto;
import com.commerce.pagopa.domain.image.service.ImageService;
import com.commerce.pagopa.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageApiController {

    private final ImageService imageService;

    @PostMapping("/upload/{category}")
    public ResponseEntity<ApiResponse<ImageResponseDto>> upload(
            @PathVariable("category") String category,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(imageService.upload(
                        file,
                        ImageCategory.valueOf(category.toUpperCase()))
                )
        );
    }

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
