package com.commerce.pagopa.assistant.ai.presentation;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springdoc.core.annotations.ParameterObject;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;

import com.commerce.pagopa.assistant.ai.application.AssistantChatService;
import com.commerce.pagopa.assistant.ai.application.AssistantConversationService;
import com.commerce.pagopa.assistant.ai.application.AssistantHistoryService;
import com.commerce.pagopa.assistant.ai.application.dto.request.AssistantConversationRequestDto;
import com.commerce.pagopa.assistant.ai.application.dto.request.AssistantMessageRequestDto;
import com.commerce.pagopa.assistant.ai.application.dto.response.*;
import com.commerce.pagopa.assistant.application.dto.response.*;
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

    @PostMapping(
            value = "/{conversationId}/messages/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<ServerSentEvent<AssistantStreamEventResponseDto>> chatStream(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long conversationId,
            @Valid @RequestBody AssistantMessageRequestDto requestDto
    ) {
        return assistantChatService.chatStream(
                userId,
                conversationId,
                requestDto
        )
                .map(
                        event -> ServerSentEvent.<AssistantStreamEventResponseDto>builder()
                                .event(event.type())
                                .data(event)
                                .build()
                );
    }

    // @DeleteMapping("/{conversationId}")
}
