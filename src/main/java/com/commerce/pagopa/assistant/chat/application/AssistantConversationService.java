package com.commerce.pagopa.assistant.chat.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.assistant.chat.application.dto.request.AssistantConversationRequestDto;
import com.commerce.pagopa.assistant.chat.application.dto.response.AssistantConversationResponseDto;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversation;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversationRepository;
import com.commerce.pagopa.global.response.OffsetPageResponseDto;


@Service
@RequiredArgsConstructor
public class AssistantConversationService {

    private final AssistantConversationRepository conversationRepository;

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
    public OffsetPageResponseDto<AssistantConversationResponseDto> getAll(Long userId, Pageable pageable) {
        Page<AssistantConversation> conversations =
                conversationRepository.findAllByUserIdOrderByUpdatedAtDesc(
                        userId,
                        pageable
                );

        return OffsetPageResponseDto.from(
                conversations.map(AssistantConversationResponseDto::from)
        );
    }
}
