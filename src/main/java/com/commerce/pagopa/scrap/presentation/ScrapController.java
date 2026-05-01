package com.commerce.pagopa.scrap.presentation;

import com.commerce.pagopa.global.entity.CustomUserDetails;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.scrap.application.ScrapService;
import com.commerce.pagopa.scrap.application.dto.request.ScrapAddRequestDto;
import com.commerce.pagopa.scrap.application.dto.response.ScrapResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/scraps")
public class ScrapController {

    private final ScrapService scrapService;

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

    @DeleteMapping("/{id}")
    @PreAuthorize("@scrapOwnerValidator.isOwner(#scrapId, principal.userId)")
    public ResponseEntity<ApiResponse<Void>> deleteScrap(@PathVariable("id") Long scrapId) {
        scrapService.delete(scrapId);
        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }

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
