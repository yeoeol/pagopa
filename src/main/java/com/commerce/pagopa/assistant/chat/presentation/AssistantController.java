package com.commerce.pagopa.assistant.chat.presentation;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;

import com.commerce.pagopa.assistant.chat.application.AssistantChatService;
import com.commerce.pagopa.assistant.chat.application.AssistantConversationService;
import com.commerce.pagopa.assistant.chat.application.AssistantHistoryService;
import com.commerce.pagopa.assistant.chat.application.dto.request.AssistantConversationRequestDto;
import com.commerce.pagopa.assistant.chat.application.dto.request.AssistantMessageRequestDto;
import com.commerce.pagopa.assistant.chat.application.dto.response.*;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.global.response.OffsetPageResponseDto;

@Tag(
        name = "ASSISTANT CHAT API",
        description = "어시스턴트 대화 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assistant/conversations")
public class AssistantController {

    private final AssistantConversationService conversationService;
    private final AssistantHistoryService historyService;
    private final AssistantChatService assistantChatService;

    @Operation(
            summary = "어시스턴트 대화 생성",
            description = "어시스턴트 대화를 생성합니다."
    )
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

    @Operation(
            summary = "어시스턴트 활성 대화방 목록 조회",
            description = "활성화된 어시스턴트 대화방 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<OffsetPageResponseDto<AssistantConversationResponseDto>>> getAll(
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
                        conversationService.getAllWithActive(
                                userId,
                                pageable
                        )
                )
        );
    }

    @Operation(
            summary = "어시스턴트 대화 메시지 목록 조회",
            description = "어시스턴트 대화 메시지 목록을 전체 조회합니다."
    )
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

    @Operation(
            summary = "어시스턴트 대화 메시지 송신/응답",
            description = "어시스턴트에게 메시지를 보내고 응답받습니다."
    )
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

    @Operation(
            summary = "[실시간] 어시스턴트 대화 메시지 송신/응답",
            description = "어시스턴트에게 메시지를 보내고, 토큰 단위 응답을 실시간으로 받습니다."
    )
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

    @Operation(
            summary = "어시스턴트 대화방 종료",
            description = "어시스턴트 대화방을 종료(CLOSED) 상태로 변경하고 대화방 목록에서 제외합니다. 복원을 위해 삭제하지 않습니다."
    )
    @PatchMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<AssistantConversationResponseDto>> close(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable @Positive Long conversationId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        conversationService.close(
                                userId,
                                conversationId
                        )
                )
        );
    }

    @Operation(
            summary = "어시스턴트 대화 메시지 삭제",
            description = "본인 대화방의 본인(USER) 메시지를 삭제합니다. 종료된 대화방의 메시지는 삭제할 수 없습니다."
    )
    @DeleteMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable @Positive Long conversationId,
            @PathVariable @Positive Long messageId
    ) {
        historyService.delete(
                userId,
                conversationId,
                messageId
        );
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
