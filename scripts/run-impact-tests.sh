#!/usr/bin/env bash
# Runs a repeatable authenticated API benchmark, then snapshots measured product impact.
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
output_dir="${OUTPUT_DIR:-$repo_root/artifacts/impact}"
timestamp="$(date -u +%Y%m%dT%H%M%SZ)"

if ! command -v k6 >/dev/null 2>&1; then
  echo "k6 is required for the performance portion. Install it, then rerun: https://grafana.com/docs/k6/latest/set-up/install-k6/" >&2
  exit 1
fi

mkdir -p "$output_dir"
BASE_URL="${BASE_URL:-http://localhost:8080}" \
  k6 run --summary-export "$output_dir/performance-$timestamp.json" "$repo_root/qa/impact/performance.k6.js"
"$repo_root/scripts/generate-impact-report.sh"
