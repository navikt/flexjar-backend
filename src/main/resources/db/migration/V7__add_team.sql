ALTER TABLE feedback
    ADD team VARCHAR(255) DEFAULT 'flex';
ALTER TABLE feedback
    ADD app VARCHAR(255) NULL;

UPDATE feedback
SET app = feedbackJson ->> 'app'
WHERE feedbackJson ->> 'app' IS NOT NULL;
