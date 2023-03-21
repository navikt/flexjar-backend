ALTER TABLE feilmelding
    ADD COLUMN method VARCHAR(10),
    ADD COLUMN response_code INT,
    ADD COLUMN content_length INT;