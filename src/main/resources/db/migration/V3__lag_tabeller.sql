CREATE TABLE payloads
(
    id           VARCHAR DEFAULT uuid_generate_v4() PRIMARY KEY,
    frontend_app VARCHAR NOT NULL,
    opprettet    TIMESTAMP WITH TIME ZONE,
    x_request_id VARCHAR NOT NULL,
    json_payload VARCHAR NOT NULL
);

CREATE INDEX i_payloads_frontend_app_opprettet ON payloads (frontend_app, opprettet);
CREATE INDEX i_payloads_x_request_id ON payloads (x_request_id);