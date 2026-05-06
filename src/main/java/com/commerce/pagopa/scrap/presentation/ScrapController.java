package com.commerce.pagopa.scrap.presentation;

import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.scrap.application.ScrapService;
import com.commerce.pagopa.scrap.application.dto.request.ScrapAddRequestDto;
import com.commerce.pagopa.scrap.application.dto.response.ScrapResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SCRAP API", description = "스크랩 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/scraps")
public class ScrapController {

    private final ScrapService scrapService;

    @Operation(summary = "스크랩 추가", description = "특정 게시글을 스크랩합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ScrapResponseDto>> addScrap(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ScrapAddRequestDto requestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        scrapService.addScrap(userDetails.getUserId(), requestDto))
                );
    }

    @Operation(summary = "스크랩 삭제", description = "스크랩을 삭제합니다.")
    @DeleteMapping("/{id}")
    @PreAuthorize("@scrapOwnerValidator.isOwner(#scrapId, principal.userId)")
    public ResponseEntity<ApiResponse<Void>> deleteScrap(@PathVariable("id") Long scrapId) {
        scrapService.delete(scrapId);
        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }

    @Operation(summary = "스크랩 목록 조회", description = "스크랩 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScrapResponseDto>>> getScraps(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ScrapResponseDto> responses = scrapService.findAllByUser(userDetails.getUserId());
        return ResponseEntity.ok(
                ApiResponse.ok(responses)
        );
    }
}
