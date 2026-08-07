-- Safe, self-contained benchmark. The temporary table disappears at session end.
CREATE TEMP TABLE benchmark_request_history (id bigint GENERATED ALWAYS AS IDENTITY, endpoint_id uuid NOT NULL, response_time_ms bigint NOT NULL);
INSERT INTO benchmark_request_history(endpoint_id, response_time_ms)
SELECT CASE WHEN n = 100000 THEN '00000000-0000-0000-0000-000000000777'::uuid ELSE md5((n % 1000)::text)::uuid END, n % 5000
FROM generate_series(1, 200000) AS n;
ANALYZE benchmark_request_history;
BEGIN;
SET LOCAL enable_indexscan = off;
SET LOCAL enable_bitmapscan = off;
EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) SELECT count(*) FROM benchmark_request_history WHERE endpoint_id = '00000000-0000-0000-0000-000000000777'::uuid;
ROLLBACK;
CREATE INDEX benchmark_request_history_endpoint_idx ON benchmark_request_history(endpoint_id);
ANALYZE benchmark_request_history;
RESET enable_indexscan;
RESET enable_bitmapscan;
EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) SELECT count(*) FROM benchmark_request_history WHERE endpoint_id = '00000000-0000-0000-0000-000000000777'::uuid;
