ALTER TABLE feedback
    ADD team VARCHAR(255) DEFAULT 'flex';
ALTER TABLE feedback
    ADD app VARCHAR(255) NULL;

