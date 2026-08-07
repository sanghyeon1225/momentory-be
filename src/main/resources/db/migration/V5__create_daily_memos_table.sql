CREATE TABLE daily_memos (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    memo_date DATE NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_daily_memos_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_daily_memos_user_memo_date
        UNIQUE (user_id, memo_date)
);
