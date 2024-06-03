UPDATE feedback
SET feedback_json = jsonb_set(feedback_json::jsonb, '{feedbackId}', '"sykpengesoknad-gjenapne-survey"')::text
WHERE (feedback_json::json ->> 'feedbackId') = 'sykpengesoknad-avbryt-survey'
  AND opprettet >= '2024-05-27';
