package com.commerce.pagopa.assistant.chat.presentation;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.commerce.pagopa.assistant.chat.application.AssistantChatService;
import com.commerce.pagopa.assistant.chat.application.AssistantConversationService;
import com.commerce.pagopa.assistant.chat.application.AssistantHistoryService;
import com.commerce.pagopa.assistant.chat.application.dto.response.AssistantConversationResponseDto;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.identity.domain.RoleCode;
import com.commerce.pagopa.identity.infrastructure.security.CustomUserDetails;
import com.commerce.pagopa.support.ControllerSliceTest;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerSliceTest(AssistantController.class)
class AssistantControllerTest {

    private static final Long USER_ID = 1L;
    private static final String CLOSE_URL = "/api/v1/assistant/conversations/{conversationId}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssistantConversationService conversationService;

    @MockitoBean
    private AssistantHistoryService historyService;

    @MockitoBean
    private AssistantChatService assistantChatService;

    // ControllerSliceTest는 addFilters=false라 보안 필터가 SecurityContext를 채우지 않으므로 직접 설정한다.
    @BeforeEach
    void setUpAuthentication() {
        CustomUserDetails principal = new CustomUserDetails(
                USER_ID,
                "user@test.com",
                Set.of(RoleCode.ROLE_USER)
        );
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        )
                );
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("대화방 종료 성공 시 200과 CLOSED 상태를 반환한다")
    void close_success() throws Exception {
        given(conversationService.close(USER_ID, 10L)).willReturn(
                new AssistantConversationResponseDto(
                        10L,
                        USER_ID,
                        "배송 문의",
                        "CLOSED"
                )
        );

        mockMvc.perform(patch(CLOSE_URL, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.conversationId").value(10))
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        verify(conversationService).close(USER_ID, 10L);
    }

    @Test
    @DisplayName("존재하지 않는 대화방이면 404를 반환한다")
    void close_notFound() throws Exception {
        given(conversationService.close(USER_ID, 999L)).willThrow(
                new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_NOT_FOUND)
        );

        mockMvc.perform(patch(CLOSE_URL, 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ASSISTANT_CONVERSATION_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("이미 종료된 대화방이면 409를 반환한다")
    void close_alreadyClosed_conflict() throws Exception {
        given(conversationService.close(USER_ID, 10L)).willThrow(
                new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_CLOSED)
        );

        mockMvc.perform(patch(CLOSE_URL, 10L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ASSISTANT_CONVERSATION_CLOSED.getCode()));
    }

    @ParameterizedTest(name = "conversationId={0}")
    @ValueSource(strings = {"0", "-1"})
    @DisplayName("0 이하의 id면 400을 반환하고 서비스를 호출하지 않는다")
    void close_nonPositiveId_badRequest(String conversationId) throws Exception {
        mockMvc.perform(patch(CLOSE_URL, conversationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()));

        verifyNoInteractions(conversationService);
    }

    @ParameterizedTest(name = "conversationId={0}")
    @ValueSource(strings = {"abc", "1.5", "99999999999999999999"})
    @DisplayName("숫자(Long)로 변환할 수 없는 id면 400을 반환하고 서비스를 호출하지 않는다")
    void close_invalidTypeId_badRequest(String conversationId) throws Exception {
        mockMvc.perform(patch(CLOSE_URL, conversationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_TYPE_VALUE.getCode()));

        verifyNoInteractions(conversationService);
    }
}
