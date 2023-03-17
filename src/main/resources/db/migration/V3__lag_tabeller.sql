CREATE TABLE feilmelding
(
    id         VARCHAR DEFAULT uuid_generate_v4() PRIMARY KEY,
    opprettet  TIMESTAMP WITH TIME ZONE,
    request_id VARCHAR NOT NULL,
    app        VARCHAR NOT NULL,
    payload    VARCHAR NOT NULL
);

CREATE INDEX i_feilmelding_frontend_app_opprettet ON feilmelding (app, opprettet DESC);
CREATE INDEX i_feilmelding_request_id ON feilmelding (request_id);