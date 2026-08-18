ALTER TABLE form_observation_snapshots
    ADD COLUMN preparation_session_id BIGINT
        REFERENCES application_preparation_sessions(id);

CREATE INDEX ix_form_observation_snapshots_session
    ON form_observation_snapshots(preparation_session_id)
    WHERE preparation_session_id IS NOT NULL;
