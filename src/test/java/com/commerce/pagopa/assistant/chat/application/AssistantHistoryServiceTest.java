package com.commerce.pagopa.assistant.chat.application;

import java.util.Optional;

import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.commerce.pagopa.assistant.chat.domain.AssistantConversation;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversationRepository;
import com.commerce.pagopa.assistant.chat.domain.AssistantMessage;
import com.commerce.pagopa.assistant.chat.domain.AssistantMessageRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AssistantHistoryServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long CONVERSATION_ID = 10L;
    private static final Long OTHER_CONVERSATION_ID = 20L;
    private static final Long MESSAGE_ID = 100L;

    private AssistantConversationRepository conversationRepository;
    private AssistantMessageRepository messageRepository;
    private AssistantHistoryService historyService;

    @BeforeEach
    void setUp() {
        conversationRepository = mock(AssistantConversationRepository.class);
        messageRepository = mock(AssistantMessageRepository.class);
        // default 메서드는 mock이 null을 반환하므로 실제 구현(findById + orElseThrow)을 타게 한다
        given(messageRepository.findByIdOrThrow(any())).willCallRealMethod();
        historyService = new AssistantHistoryService(conversationRepository, messageRepository);
    }

    @Test
    @DisplayName("본인 대화방의 본인(USER) 메시지를 삭제하면 repository에서 하드 삭제한다")
    void delete_ownUserMessage_deletesMessage() {
        AssistantConversation conversation = conversation(CONVERSATION_ID);
        AssistantMessage message = AssistantMessage.user(conversation, "배송 언제 와요?");
        givenConversation(conversation);
        given(messageRepository.findById(MESSAGE_ID)).willReturn(Optional.of(message));

        historyService.delete(USER_ID, CONVERSATION_ID, MESSAGE_ID);

        verify(messageRepository).delete(message);
    }

    @Test
    @DisplayName("존재하지 않거나 본인 소유가 아닌 대화방이면 ASSISTANT_CONVERSATION_NOT_FOUND 예외를 던진다")
    void delete_conversationNotFound_throwsBusinessException() {
        given(conversationRepository.findByIdAndUserIdForUpdate(CONVERSATION_ID, USER_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> historyService.delete(USER_ID, CONVERSATION_ID, MESSAGE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ASSISTANT_CONVERSATION_NOT_FOUND);
        verify(messageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("종료된 대화방이면 메시지 조회 전에 ASSISTANT_CONVERSATION_CLOSED 예외를 던진다")
    void delete_closedConversation_throwsBusinessException() {
        AssistantConversation conversation = conversation(CONVERSATION_ID);
        conversation.close();
        givenConversation(conversation);

        assertThatThrownBy(() -> historyService.delete(USER_ID, CONVERSATION_ID, MESSAGE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ASSISTANT_CONVERSATION_CLOSED);
        verify(messageRepository, never()).findById(any());
        verify(messageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("메시지가 없으면(이미 삭제된 경우 포함) ASSISTANT_MESSAGE_NOT_FOUND 예외를 던진다")
    void delete_messageNotFound_throwsBusinessException() {
        givenConversation(conversation(CONVERSATION_ID));
        given(messageRepository.findById(MESSAGE_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> historyService.delete(USER_ID, CONVERSATION_ID, MESSAGE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ASSISTANT_MESSAGE_NOT_FOUND);
        verify(messageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("다른 대화방 소속 메시지면 ASSISTANT_MESSAGE_NOT_OWNER 예외를 던진다")
    void delete_messageOfOtherConversation_throwsBusinessException() {
        givenConversation(conversation(CONVERSATION_ID));
        AssistantMessage message = AssistantMessage.user(conversation(OTHER_CONVERSATION_ID), "다른 방 메시지");
        given(messageRepository.findById(MESSAGE_ID)).willReturn(Optional.of(message));

        assertThatThrownBy(() -> historyService.delete(USER_ID, CONVERSATION_ID, MESSAGE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ASSISTANT_MESSAGE_NOT_OWNER);
        verify(messageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("ASSISTANT 역할 메시지면 ASSISTANT_MESSAGE_NOT_OWNER 예외를 던진다")
    void delete_assistantRoleMessage_throwsBusinessException() {
        AssistantConversation conversation = conversation(CONVERSATION_ID);
        givenConversation(conversation);
        AssistantMessage message = AssistantMessage.assistant(conversation, "내일 도착 예정입니다.", 10L, 20L);
        given(messageRepository.findById(MESSAGE_ID)).willReturn(Optional.of(message));

        assertThatThrownBy(() -> historyService.delete(USER_ID, CONVERSATION_ID, MESSAGE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ASSISTANT_MESSAGE_NOT_OWNER);
        verify(messageRepository, never()).delete(any());
    }

    private AssistantConversation conversation(Long conversationId) {
        AssistantConversation conversation = AssistantConversation.start(USER_ID, "배송 문의");
        ReflectionTestUtils.setField(conversation, "id", conversationId);
        return conversation;
    }

    private void givenConversation(AssistantConversation conversation) {
        given(conversationRepository.findByIdAndUserIdForUpdate(CONVERSATION_ID, USER_ID))
                .willReturn(Optional.of(conversation));
    }
}
