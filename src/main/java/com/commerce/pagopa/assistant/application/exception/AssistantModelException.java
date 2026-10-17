package com.commerce.pagopa.assistant.application.exception;

public class AssistantModelException extends RuntimeException {

    public AssistantModelException(String message) {
        super(message);
    }

    public AssistantModelException(
            String message,
            Throwable cause
    ) {
        super(
                message,
                cause
        );
    }
}
