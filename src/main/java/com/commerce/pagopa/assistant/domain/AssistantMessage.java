package com.commerce.pagopa.assistant.domain;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.commerce.pagopa.global.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "assistant_message")
public class AssistantMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            length = 20,
            nullable = false
    )
    private AssistantMessageRole role;

    @Lob
    @Column(
            name = "content",
            nullable = false
    )
    private String content;

    @Column(name = "input_tokens")
    private Long inputTokens;

    @Column(name = "output_tokens")
    private Long outputTokens;

    @Builder(access = AccessLevel.PRIVATE)
    private AssistantMessage(
            AssistantConversation conversation,
            AssistantMessageRole role,
            String content,
            Long inputTokens,
            Long outputTokens
    ) {
        this.conversation = conversation;
        this.role = role;
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
                .content(content)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .build();
    }
}
