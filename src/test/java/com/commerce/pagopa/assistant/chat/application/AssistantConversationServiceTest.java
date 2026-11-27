package com.commerce.pagopa.assistant.chat.application;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.commerce.pagopa.assistant.chat.application.dto.response.AssistantConversationResponseDto;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversation;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversationRepository;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversationStatus;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AssistantConversationServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long CONVERSATION_ID = 10L;

    private AssistantConversationRepository conversationRepository;
    private AssistantConversationService conversationService;

    @BeforeEach
    void setUp() {
        conversationRepository = mock(AssistantConversationRepository.class);
        conversationService = new AssistantConversationService(conversationRepository);
    }

    @Test
    @DisplayName("본인 대화방을 종료하면 삭제하지 않고 상태만 CLOSED로 변경한다")
    void close_changesStatusToClosed() {
        AssistantConversation conversation = AssistantConversation.start(USER_ID, "배송 문의");
        given(conversationRepository.findByIdAndUserId(CONVERSATION_ID, USER_ID))
                .willReturn(Optional.of(conversation));

        AssistantConversationResponseDto response = conversationService.close(USER_ID, CONVERSATION_ID);

        assertThat(conversation.getStatus()).isEqualTo(AssistantConversationStatus.CLOSED);
        assertThat(response.status()).isEqualTo(AssistantConversationStatus.CLOSED.name());
    }

    @Test
    @DisplayName("이미 종료된 대화방을 다시 종료하면 ASSISTANT_CONVERSATION_CLOSED 예외를 던진다")
    void close_alreadyClosed_throwsBusinessException() {
        AssistantConversation conversation = AssistantConversation.start(USER_ID, "배송 문의");
        conversation.close();
        given(conversationRepository.findByIdAndUserId(CONVERSATION_ID, USER_ID))
                .willReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.close(USER_ID, CONVERSATION_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ASSISTANT_CONVERSATION_CLOSED);
    }

    @Test
    @DisplayName("존재하지 않거나 본인 소유가 아닌 대화방이면 ASSISTANT_CONVERSATION_NOT_FOUND 예외를 던진다")
    void close_notFound_throwsBusinessException() {
        given(conversationRepository.findByIdAndUserId(CONVERSATION_ID, USER_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.close(USER_ID, CONVERSATION_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ASSISTANT_CONVERSATION_NOT_FOUND);
    }

    @Test
    @DisplayName("대화방 목록은 ACTIVE 상태만 조회한다")
    void getAll_queriesOnlyActiveConversations() {
        Pageable pageable = PageRequest.of(0, 10);
        given(
                conversationRepository.findAllByUserIdAndStatusOrderByUpdatedAtDesc(
                        USER_ID,
                        AssistantConversationStatus.ACTIVE,
                        pageable
                )
        ).willReturn(new PageImpl<>(List.of(AssistantConversation.start(USER_ID, "배송 문의")), pageable, 1));

        conversationService.getAllWithActive(USER_ID, pageable);

        verify(conversationRepository).findAllByUserIdAndStatusOrderByUpdatedAtDesc(
                USER_ID,
                AssistantConversationStatus.ACTIVE,
                pageable
        );
    }
}
