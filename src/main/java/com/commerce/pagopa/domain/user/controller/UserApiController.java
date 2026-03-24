package com.commerce.pagopa.domain.user.controller;

import com.commerce.pagopa.global.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserApiController {

    @GetMapping("/me")
    public ResponseEntity<CustomUserDetails> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        String email = userDetails.getEmail();
        String role = userDetails.getRole().name();
        log.info("userId={}, email={}, role={}", userId, email, role);
        return ResponseEntity.ok(userDetails);
    }

}
