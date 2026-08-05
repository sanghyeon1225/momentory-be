CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY,
    nickname VARCHAR(10) NOT NULL,
    age INTEGER,
    gender VARCHAR(20) NOT NULL,
    reflection_time TIME NOT NULL,
    time_zone VARCHAR(64) NOT NULL,
    calendar_integration_enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_user_profiles_age
        CHECK (age IS NULL OR age BETWEEN 1 AND 120)
);

CREATE TABLE user_interest_areas (
    user_id BIGINT NOT NULL,
    interest_area VARCHAR(30) NOT NULL,
    CONSTRAINT pk_user_interest_areas PRIMARY KEY (user_id, interest_area),
    CONSTRAINT fk_user_interest_areas_user
        FOREIGN KEY (user_id) REFERENCES user_profiles (user_id)
);
