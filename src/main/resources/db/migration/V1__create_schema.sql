CREATE SEQUENCE url_id_seq START WITH 100000 INCREMENT BY 1;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE urls (
    id BIGINT PRIMARY KEY,
    short_code VARCHAR(20) UNIQUE NOT NULL,
    original_url TEXT NOT NULL,
    user_id BIGINT REFERENCES users(id),
    is_custom_alias BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_urls_user_id ON urls(user_id);
CREATE INDEX idx_urls_active_short_code ON urls(short_code) WHERE is_deleted = FALSE;

CREATE TABLE analytics_summary (
    short_code VARCHAR(20) PRIMARY KEY,
    total_clicks BIGINT NOT NULL DEFAULT 0,
    last_clicked_at TIMESTAMP
);

CREATE TABLE daily_clicks (
    short_code VARCHAR(20) NOT NULL,
    click_date DATE NOT NULL,
    click_count BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (short_code, click_date)
);
