package com.commerce.pagopa.assistant.ai.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

import com.commerce.pagopa.global.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "assistant_conversation")
public class AssistantConversation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(
            name = "conversation_id",
            nullable = false
    )
    private Long id;

    @ToString.Include
    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @ToString.Include
    @Column(
            name = "title",
            length = 100,
            nullable = false
    )
    private String title;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            length = 20,
            nullable = false
    )
    private AssistantConversationStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    private AssistantConversation(
            Long userId,
            String title,
            AssistantConversationStatus status
    ) {
        this.userId = userId;
        this.title = title;
        this.status = status;
    }

    public static AssistantConversation start(
            Long userId,
            String title
    ) {
        return AssistantConversation.builder()
                .userId(userId)
                .title(title)
                .status(AssistantConversationStatus.ACTIVE)
                .build();
    }

    public void close() {
        this.status = AssistantConversationStatus.CLOSED;
    }

    public boolean isActive() {
        return this.status == AssistantConversationStatus.ACTIVE;
    }

    public void renewUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}
