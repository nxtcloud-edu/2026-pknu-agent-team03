CREATE TABLE IF NOT EXISTS backup_changes (
    change_id VARCHAR(128) PRIMARY KEY,
    anonymous_user_id VARCHAR(128) NOT NULL,
    entity_type VARCHAR(32) NOT NULL,
    entity_id VARCHAR(128) NOT NULL,
    operation VARCHAR(16) NOT NULL,
    occurred_at BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_backup_user
    ON backup_changes (anonymous_user_id);

CREATE TABLE IF NOT EXISTS retention_settings (
    anonymous_user_id VARCHAR(128) PRIMARY KEY,
    selection VARCHAR(32) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS deletion_jobs (
    job_id VARCHAR(128) PRIMARY KEY,
    anonymous_user_id VARCHAR(128) NOT NULL,
    requested_at BIGINT NOT NULL,
    server_status VARCHAR(32) NOT NULL,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_deletion_user
    ON deletion_jobs (anonymous_user_id);
