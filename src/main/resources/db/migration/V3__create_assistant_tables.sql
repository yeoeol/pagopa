CREATE TABLE assistant_conversation (
    conversation_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (conversation_id),
    CONSTRAINT fk_assistant_conversation_user FOREIGN KEY (user_id)
        REFERENCES user (user_id)
);

CREATE TABLE assistant_message (
    message_id BIGINT NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    input_tokens BIGINT NULL,
    output_tokens BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (message_id),
    CONSTRAINT fk_assistant_conversation_conversation FOREIGN KEY (conversation_id)
        REFERENCES assistant_conversation (conversation_id)
);
