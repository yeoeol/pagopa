package com.commerce.pagopa.assistant.chat.domain;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.*;

import com.commerce.pagopa.global.entity.BaseTimeEntity;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "assistant_message")
public class AssistantMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(
            name = "message_id",
            nullable = false
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "conversation_id",
            nullable = false
    )
    private AssistantConversation conversation;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            length = 20,
            nullable = false
    )
    private AssistantMessageRole role;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            length = 20,
            nullable = false
    )
    private AssistantMessageStatus status;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(
            name = "content",
            nullable = false
    )
    private String content;

    @ToString.Include
    @Column(name = "input_tokens")
    private Long inputTokens;

    @ToString.Include
    @Column(name = "output_tokens")
    private Long outputTokens;

    @Builder(access = AccessLevel.PRIVATE)
    private AssistantMessage(
            AssistantConversation conversation,
            AssistantMessageRole role,
            AssistantMessageStatus status,
            String content,
            Long inputTokens,
            Long outputTokens
    ) {
        this.conversation = conversation;
        this.role = role;
        this.status = status;
        this.content = content;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }

    public static AssistantMessage user(
            AssistantConversation conversation,
            String content
    ) {
        return AssistantMessage.builder()
                .conversation(conversation)
                .role(AssistantMessageRole.USER)
                .status(AssistantMessageStatus.SUCCESS)
                .content(content)
                .inputTokens(null)
                .outputTokens(null)
                .build();
    }

    public static AssistantMessage assistant(
            AssistantConversation conversation,
            String content,
            long inputTokens,
            long outputTokens
    ) {
        return AssistantMessage.builder()
                .conversation(conversation)
                .role(AssistantMessageRole.ASSISTANT)
                .status(AssistantMessageStatus.SUCCESS)
                .content(content)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .build();
    }

    public void fail() {
        if (this.status == AssistantMessageStatus.FAILED) {
            throw new BusinessException(ErrorCode.ASSISTANT_MESSAGE_ALREADY_FAILED);
        }
        this.status = AssistantMessageStatus.FAILED;
    }
}
