ALTER TABLE observed_questions
    ADD COLUMN page_key VARCHAR(200) NOT NULL DEFAULT 'application';

ALTER TABLE observed_questions
    ALTER COLUMN page_key DROP DEFAULT;
