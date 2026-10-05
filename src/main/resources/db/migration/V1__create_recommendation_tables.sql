CREATE TABLE recommendation_event(
    event_id        VARCHAR(50) NOT NULL,
    event_type      VARCHAR(50) NOT NULL,
    user_id         BIGINT NOT NULL,
    aggregate_id    BIGINT NULL,
    payload         JSON NOT NULL,
    occurred_at     DATETIME NOT NULL,
    processed_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY(event_id),

    CONSTRAINT uq_recommendation_event_business
    UNIQUE(event_type, aggregate_id),

    INDEX idx_recommendation_event_processed_at (
        processed_at
    )
);

CREATE TABLE recommendation_interest (
    user_id         BIGINT NOT NULL,
    interest_type   VARCHAR(20) NOT NULL,
    interest_key    VARCHAR(100) NOT NULL,
    score           SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    last_event_at   DATETIME NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (
        user_id,
        interest_type,
        interest_key
     ),

    INDEX idx_recommendation_interest_ranking (
        user_id,
        interest_type,
        score DESC
    )
);

