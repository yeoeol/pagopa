package com.commerce.pagopa.assistant.ai.application;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.commerce.pagopa.assistant.ai.application.dto.request.AssistantMessageRequestDto;
import com.commerce.pagopa.assistant.ai.application.dto.response.AssistantHistoryMessageResponseDto;
import com.commerce.pagopa.assistant.ai.application.dto.response.AssistantMessageResponseDto;
import com.commerce.pagopa.assistant.ai.application.dto.response.AssistantStreamEventResponseDto;
import com.commerce.pagopa.assistant.ai.application.dto.response.GeneratedAnswer;
import com.commerce.pagopa.assistant.ai.application.exception.AssistantModelException;
import com.commerce.pagopa.assistant.application.dto.response.*;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

@Service
@RequiredArgsConstructor
public class AssistantChatService {

    private final AssistantAnswerGenerator answerGenerator;
    private final AssistantHistoryService historyService;

    public AssistantMessageResponseDto chat(
            Long userId,
            Long conversationId,
            AssistantMessageRequestDto requestDto
    ) {
        AssistantHistoryMessageResponseDto userMessage = historyService.saveUserMessage(
                userId,
                conversationId,
                requestDto.message()
        );

        try {
            GeneratedAnswer answer = answerGenerator.generate(
                    conversationId,
                    requestDto.message()
            );

            AssistantHistoryMessageResponseDto assistantMessage = historyService.saveAssistantMessage(
                    conversationId,
                    answer
            );

            return new AssistantMessageResponseDto(
                    conversationId,
                    userMessage.messageId(),
                    assistantMessage.messageId(),
                    answer.content(),
                    answer.inputTokens(),
                    answer.outputTokens()
            );
        } catch (AssistantModelException exception) {
            historyService.markGenerationFailed(userMessage.messageId());
            throw new BusinessException(
                    ErrorCode.ASSISTANT_MODEL_UNAVAILABLE,
                    exception.getMessage()
            );
        }
    }

    public Flux<AssistantStreamEventResponseDto> chatStream(
            Long userId,
            Long conversationId,
            AssistantMessageRequestDto requestDto
    ) {
        AssistantHistoryMessageResponseDto userMessage = historyService.saveUserMessage(
                userId,
                conversationId,
                requestDto.message()
        );

        return Flux.defer(() -> {
            StringBuilder contentBuffer = new StringBuilder();
            AtomicLong inputTokens = new AtomicLong();
            AtomicLong outputTokens = new AtomicLong();

            Flux<AssistantStreamEventResponseDto> deltas = answerGenerator.generateStream(
                    conversationId,
                    requestDto.message()
            )
                    // 답변 조각 하나가 전달될 때마다 실행
                    .doOnNext(chunk -> {
                        contentBuffer.append(chunk.content());
                        inputTokens.accumulateAndGet(
                                chunk.inputTokens(),
                                Math::max
                        );
                        outputTokens.accumulateAndGet(
                                chunk.outputTokens(),
                                Math::max
                        );
                    })
                    .filter(chunk -> !chunk.content().isEmpty())
                    .map(
                            chunk -> AssistantStreamEventResponseDto.delta(
                                    chunk.content()
                            )
                    );

            Mono<AssistantStreamEventResponseDto> completed = Mono
                    .fromCallable(
                            () -> historyService.saveAssistantMessage(
                                    conversationId,
                                    new GeneratedAnswer(
                                            contentBuffer.toString(),
                                            inputTokens.get(),
                                            outputTokens.get()
                                    )
                            )
                    )
                    // JPA는 블로킹 방식이므로 상류 스트림의 완료 신호를 처리하는
                    // 스레드를 점유하지 않도록 boundedElastic에서 실행
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(
                            saved -> AssistantStreamEventResponseDto.completed(
                                    conversationId,
                                    userMessage.messageId(),
                                    saved.messageId(),
                                    saved.inputTokens(),
                                    saved.outputTokens()
                            )
                    );

            return Flux.concat(
                    Mono.just(
                            AssistantStreamEventResponseDto.started(
                                    conversationId,
                                    userMessage.messageId()
                            )
                    ),
                    deltas,
                    completed
            )
                    .onErrorResume(
                            exception -> Mono.fromCallable(() -> {
                                historyService.markGenerationFailed(
                                        userMessage.messageId()
                                );
                                return AssistantStreamEventResponseDto.failed(
                                        ErrorCode.ASSISTANT_MODEL_UNAVAILABLE.getCode()
                                );
                            })
                                    .subscribeOn(Schedulers.boundedElastic())
                    );
        });
    }
}
