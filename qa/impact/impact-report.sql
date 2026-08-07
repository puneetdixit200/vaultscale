-- VaultScale impact report: counts real persisted outcomes; it does not infer
-- revenue, users reached, or security incidents that are not stored by the app.
WITH execution AS (
    SELECT
        COUNT(*) AS total_runs,
        COUNT(*) FILTER (WHERE executed_at >= NOW() - INTERVAL '30 days') AS runs_last_30_days,
        COUNT(*) FILTER (WHERE error_message IS NULL AND status_code BETWEEN 200 AND 399) AS successful_runs,
        COUNT(*) FILTER (WHERE error_message IS NOT NULL OR status_code IS NULL OR status_code >= 400) AS unsuccessful_runs,
        COUNT(*) FILTER (
            WHERE error_message ~* '(private|internal|loopback|link-local|site-local|malformed|only http|valid host|resolve)'
        ) AS ssrf_or_url_blocks,
        ROUND((PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY response_time_ms))::numeric, 2) AS p50_response_time_ms,
        ROUND((PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY response_time_ms))::numeric, 2) AS p95_response_time_ms
    FROM request_history
),
audit AS (
    SELECT COALESCE(json_object_agg(action, action_count), '{}'::json) AS actions
    FROM (
        SELECT action, COUNT(*) AS action_count
        FROM audit_logs
        GROUP BY action
        ORDER BY action
    ) counts
)
SELECT json_build_object(
    'schema', 'vaultscale-impact-report/v1',
    'generated_at_utc', TO_CHAR(NOW() AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
    'product_usage', json_build_object(
        'registered_users', (SELECT COUNT(*) FROM users),
        'active_users', (SELECT COUNT(*) FROM users WHERE is_active),
        'organizations', (SELECT COUNT(*) FROM organizations),
        'organization_memberships', (SELECT COUNT(*) FROM org_memberships),
        'collections', (SELECT COUNT(*) FROM collections),
        'saved_endpoints', (SELECT COUNT(*) FROM endpoints)
    ),
    'request_execution', json_build_object(
        'total_runs', execution.total_runs,
        'runs_last_30_days', execution.runs_last_30_days,
        'successful_runs', execution.successful_runs,
        'unsuccessful_runs', execution.unsuccessful_runs,
        'success_rate_pct', COALESCE(ROUND(100.0 * execution.successful_runs / NULLIF(execution.total_runs, 0), 2), 0),
        'p50_response_time_ms', COALESCE(execution.p50_response_time_ms, 0),
        'p95_response_time_ms', COALESCE(execution.p95_response_time_ms, 0)
    ),
    'security_outcomes', json_build_object(
        'ssrf_or_invalid_url_blocks', execution.ssrf_or_url_blocks,
        'classification_note', 'Best-effort count based on persisted request-history error messages.'
    ),
    'audit_events_by_action', audit.actions
) AS report
FROM execution CROSS JOIN audit;
