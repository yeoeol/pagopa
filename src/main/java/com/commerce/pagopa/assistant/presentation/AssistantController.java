package com.commerce.pagopa.assistant.presentation;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springdoc.core.annotations.ParameterObject;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.assistant.application.AssistantChatService;
import com.commerce.pagopa.assistant.application.AssistantConversationService;
import com.commerce.pagopa.assistant.application.AssistantHistoryService;
import com.commerce.pagopa.assistant.application.dto.request.AssistantConversationRequestDto;
import com.commerce.pagopa.assistant.application.dto.request.AssistantMessageRequestDto;
import com.commerce.pagopa.assistant.application.dto.response.AssistantConversationPageResponseDto;
import com.commerce.pagopa.assistant.application.dto.response.AssistantConversationResponseDto;
import com.commerce.pagopa.assistant.application.dto.response.AssistantHistoryMessageResponseDto;
import com.commerce.pagopa.assistant.application.dto.response.AssistantMessageResponseDto;
import com.commerce.pagopa.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assistant/conversations")
public class AssistantController {

    private final AssistantConversationService conversationService;
    private final AssistantHistoryService historyService;
    private final AssistantChatService assistantChatService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssistantConversationResponseDto>> create(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody AssistantConversationRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        conversationService.create(
                                userId,
                                requestDto
                        )
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AssistantConversationPageResponseDto>> getAll(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @ParameterObject
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        conversationService.getAll(
                                userId,
                                pageable
                        )
                )
        );
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<AssistantHistoryMessageResponseDto>>> getMessages(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long conversationId,
            @ParameterObject
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        historyService.getAll(
                                userId,
                                conversationId,
                                pageable
                        )
                )
        );
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<AssistantMessageResponseDto>> chat(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long conversationId,
            @Valid @RequestBody AssistantMessageRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        assistantChatService.chat(
                                userId,
                                conversationId,
                                requestDto
                        )
                )
        );
    }

    // @DeleteMapping("/{conversationId}")
}
