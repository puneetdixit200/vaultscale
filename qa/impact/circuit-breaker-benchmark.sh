#!/usr/bin/env bash
set -euo pipefail

base_url="${BASE_URL:-http://127.0.0.1:8080}"
target_url="${TARGET_URL:-}"
calls="${CALLS:-10}"

if [[ -z "$target_url" ]]; then
  cat >&2 <<'EOF'
TARGET_URL is required.

Use a controlled PUBLIC HTTP(S) endpoint that resolves successfully and then
returns 5xx or fails at the network layer. Do not use *.invalid, localhost,
private IPs, or documentation ranges: the SSRF preflight rejects those before
the circuit breaker, which would test URL validation instead of Resilience4j.

Example shape (replace with infrastructure you control):
  TARGET_URL=https://failure-target.example.com/503 qa/impact/circuit-breaker-benchmark.sh
EOF
  exit 2
fi

for tool in curl jq; do
  command -v "$tool" >/dev/null 2>&1 || { echo "missing required command: $tool" >&2; exit 2; }
done

suffix="$(date +%s%N)"
reg="$(curl -fsS -X POST "$base_url/api/v1/auth/register" -H 'Content-Type: application/json' --data-binary "{\"fullName\":\"Circuit Benchmark\",\"email\":\"circuit-$suffix@example.test\",\"password\":\"ImpactTest123!\"}")"
token="$(jq -r .token <<<"$reg")"
org="$(curl -fsS -X POST "$base_url/api/v1/orgs" -H "Authorization: Bearer $token" -H 'Content-Type: application/json' --data-binary "{\"name\":\"Circuit Benchmark\",\"slug\":\"circuit-$suffix\"}")"
org_id="$(jq -r .id <<<"$org")"
collection="$(curl -fsS -X POST "$base_url/api/v1/orgs/$org_id/collections" -H "Authorization: Bearer $token" -H 'Content-Type: application/json' --data-binary '{"name":"Circuit Benchmark"}')"
collection_id="$(jq -r .id <<<"$collection")"
endpoint_json="$(jq -nc --arg url "$target_url" '{name:"Controlled failure target",method:"GET",url:$url}')"
endpoint="$(curl -fsS -X POST "$base_url/api/v1/orgs/$org_id/collections/$collection_id/endpoints" -H "Authorization: Bearer $token" -H 'Content-Type: application/json' --data-binary "$endpoint_json")"
endpoint_id="$(jq -r .id <<<"$endpoint")"
run_url="$base_url/api/v1/orgs/$org_id/collections/$collection_id/endpoints/$endpoint_id/run"

state_value() {
  local state="$1"
  curl -fsS "$base_url/actuator/prometheus" \
    | awk -v wanted="$state" '$0 ~ /^vaultscale_circuitbreaker_state\{/ && $0 ~ /name="externalApiRunner"/ && $0 ~ ("state=\"" wanted "\"") {print $2; exit}'
}

printf 'target=%s\n' "$target_url"
printf 'initial_closed=%s initial_open=%s initial_half_open=%s\n' \
  "$(state_value CLOSED || true)" "$(state_value OPEN || true)" "$(state_value HALF_OPEN || true)"

out="$(mktemp)"
trap 'rm -f "$out" /tmp/vaultscale-circuit-*.json' EXIT

for i in $(seq 1 "$calls"); do
  response_file="/tmp/vaultscale-circuit-$i.json"
  timing="$(curl -sS -o "$response_file" -w '%{time_total}' -X POST "$run_url" -H "Authorization: Bearer $token")"
  error_message="$(jq -r '.errorMessage // empty' "$response_file")"
  printf 'call=%02d elapsed_s=%s closed=%s open=%s half_open=%s error=%q\n' \
    "$i" "$timing" \
    "$(state_value CLOSED || true)" "$(state_value OPEN || true)" "$(state_value HALF_OPEN || true)" \
    "$error_message" | tee -a "$out"
done

if ! grep -q 'circuit\\ breaker\\ open' "$out" && ! grep -q 'circuit breaker open' /tmp/vaultscale-circuit-*.json 2>/dev/null; then
  echo "ERROR: no circuit-open fallback was observed; keep the benchmark unmeasured." >&2
  exit 1
fi

if [[ "$(state_value OPEN || true)" != "1.0" && "$(state_value OPEN || true)" != "1" ]]; then
  echo "NOTE: final state is not OPEN; inspect the per-call state evidence above (the breaker may already be recovering)." >&2
fi

echo "Circuit-open behavior was observed. Preserve the raw output and calculate latency only from this controlled run."
