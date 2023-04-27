CREATE TABLE feedback
(
    id            VARCHAR DEFAULT uuid_generate_v4() PRIMARY KEY,
    opprettet     TIMESTAMP WITH TIME ZONE,
    feedback_json TEXT NOT NULL
);
