package com.commerce.pagopa.assistant.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.assistant.application.dto.request.AssistantConversationRequestDto;
import com.commerce.pagopa.assistant.application.dto.response.AssistantConversationPageResponseDto;
import com.commerce.pagopa.assistant.application.dto.response.AssistantConversationResponseDto;
import com.commerce.pagopa.assistant.domain.AssistantConversation;
import com.commerce.pagopa.assistant.domain.AssistantConversationRepository;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Service
@RequiredArgsConstructor
public class AssistantConversationService {

    private final AssistantConversationRepository conversationRepository;

    private static final int PAGE_WINDOW_SIZE = 10;

    @Transactional
    public AssistantConversationResponseDto create(Long userId, AssistantConversationRequestDto requestDto) {
        AssistantConversation conversation = conversationRepository.save(
                AssistantConversation.start(
                        userId,
                        requestDto.message()
                                .substring(
                                        0,
                                        Math.min(
                                                requestDto.message().length(),
                                                30
                                        )
                                )
                )
        );
        return AssistantConversationResponseDto.from(conversation);
    }

    @Transactional(readOnly = true)
    public AssistantConversationPageResponseDto getAll(Long userId, Pageable pageable) {
        Page<AssistantConversation> conversations = conversationRepository.findAllByUserIdOrderByUpdatedAtDesc(
                userId,
                pageable
        );

        int totalPages = conversations.getTotalPages();
        int currentPage = conversations.getNumber();
        int startPage = max(
                0,
                currentPage - PAGE_WINDOW_SIZE / 2
        );
        int endPage = min(
                max(
                        totalPages - 1,
                        0
                ),
                startPage + PAGE_WINDOW_SIZE - 1
        );
        startPage = max(
                0,
                endPage - PAGE_WINDOW_SIZE + 1
        );

        return new AssistantConversationPageResponseDto(
                conversations.getContent()
                        .stream()
                        .map(AssistantConversationResponseDto::from)
                        .toList(),
                currentPage,
                conversations.getSize(),
                conversations.getTotalElements(),
                totalPages,
                conversations.isFirst(),
                conversations.isLast(),
                startPage,
                endPage
        );

    }
}
