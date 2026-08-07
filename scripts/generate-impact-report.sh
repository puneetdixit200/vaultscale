#!/usr/bin/env bash
# Generates a JSON snapshot from the running PostgreSQL database.
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
output_dir="${OUTPUT_DIR:-$repo_root/artifacts/impact}"
timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
output_file="$output_dir/impact-$timestamp.json"

mkdir -p "$output_dir"
docker compose -f "$repo_root/docker-compose.yml" exec -T postgres \
  psql --username="${POSTGRES_USER:-vaultscale}" --dbname="${POSTGRES_DB:-vaultscale}" \
  --tuples-only --no-align --file=- < "$repo_root/qa/impact/impact-report.sql" > "$output_file"

echo "Wrote $output_file"
