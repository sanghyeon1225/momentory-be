CREATE TABLE schedules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    external_id VARCHAR(255),
    schedule_date DATE NOT NULL,
    title VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    emotion VARCHAR(30),
    display_order BIGINT NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedules_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_schedules_user_external_id
        UNIQUE (user_id, external_id)
);

CREATE INDEX idx_schedules_user_date_deleted_order
    ON schedules (user_id, schedule_date, deleted_at, display_order, id);
