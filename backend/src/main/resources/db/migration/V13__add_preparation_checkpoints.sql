ALTER TABLE application_preparation_sessions
    ADD COLUMN current_page VARCHAR(200),
    ADD COLUMN current_question VARCHAR(200),
    ADD COLUMN checkpoint TEXT,
    ADD COLUMN checkpoint_snapshot_hash VARCHAR(64),
    ADD COLUMN resume_state VARCHAR(40),
    ADD COLUMN paused_at TIMESTAMPTZ;

ALTER TABLE application_preparation_sessions
    ADD CONSTRAINT ck_preparation_checkpoint_snapshot_hash
    CHECK (checkpoint_snapshot_hash IS NULL OR checkpoint_snapshot_hash ~ '^[0-9a-f]{64}$');
