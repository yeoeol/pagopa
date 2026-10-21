package com.commerce.pagopa.assistant.knowledge.presentation.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.assistant.knowledge.application.AssistantKnowledgeSyncService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/assistant/knowledge")
public class AssistantKnowledgeAdminController {

    private final AssistantKnowledgeSyncService assistantKnowledgeSyncService;

    @PostMapping("/sync")
    public ResponseEntity<Void> syncAll() {
        assistantKnowledgeSyncService.syncAll();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sourceId}/sync")
    public ResponseEntity<Void> sync(@PathVariable String sourceId) {
        assistantKnowledgeSyncService.sync(sourceId);
        return ResponseEntity.noContent().build();
    }
}
