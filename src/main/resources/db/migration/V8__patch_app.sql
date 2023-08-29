UPDATE feedback
SET app = feedback_json::jsonb ->> 'app'
WHERE feedback_json::jsonb ->> 'app' IS NOT NULL
  AND app IS NULL;
