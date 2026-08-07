#!/usr/bin/env bash
set -euo pipefail

# Safe convenience runner. It does not stop services, drop schemas, delete
# project data, or reset shared Kafka groups.
repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
base_url="${BASE_URL:-http://127.0.0.1:8080}"
curl -fsS "$base_url/actuator/health" >/dev/null
"$repo_root/qa/impact/redis-benchmark.sh"
"$repo_root/scripts/generate-impact-report.sh"
echo "Redis and product-impact benchmarks completed."
echo "Run kafka-benchmark.sh, postgres-index-benchmark.sql, and circuit-breaker testing separately."
