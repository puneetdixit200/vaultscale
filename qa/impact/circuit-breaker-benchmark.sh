#!/usr/bin/env bash
set -euo pipefail

# Controlled Resilience4j probe. It intentionally uses a reserved .invalid DNS
# name, never a public dependency. The script reports evidence and refuses to
# invent OPEN/HALF_OPEN numbers if the fallback/state metrics are absent.
base_url="${BASE_URL:-http://127.0.0.1:8080}"
suffix="$(date +%s%N)"
reg="$(curl -fsS -X POST "$base_url/api/v1/auth/register" -H 'Content-Type: application/json' --data-binary "{\"fullName\":\"Circuit Benchmark\",\"email\":\"circuit-$suffix@example.test\",\"password\":\"ImpactTest123!\"}")"
token="$(jq -r .token <<<"$reg")"
org="$(curl -fsS -X POST "$base_url/api/v1/orgs" -H "Authorization: Bearer $token" -H 'Content-Type: application/json' --data-binary "{\"name\":\"Circuit Benchmark\",\"slug\":\"circuit-$suffix\"}")"
org_id="$(jq -r .id <<<"$org")"
collection="$(curl -fsS -X POST "$base_url/api/v1/orgs/$org_id/collections" -H "Authorization: Bearer $token" -H 'Content-Type: application/json' --data-binary '{"name":"Circuit Benchmark"}')"
collection_id="$(jq -r .id <<<"$collection")"
endpoint="$(curl -fsS -X POST "$base_url/api/v1/orgs/$org_id/collections/$collection_id/endpoints" -H "Authorization: Bearer $token" -H 'Content-Type: application/json' --data-binary '{"name":"Deterministic DNS failure","method":"GET","url":"https://vaultscale-benchmark.invalid/"}')"
endpoint_id="$(jq -r .id <<<"$endpoint")"
run_url="$base_url/api/v1/orgs/$org_id/collections/$collection_id/endpoints/$endpoint_id/run"

for i in $(seq 1 10); do
  curl -fsS -o "/tmp/vaultscale-circuit-$i.json" -w "call=$i elapsed_ms=%{time_total}\n" -X POST "$run_url" -H "Authorization: Bearer $token"
done

fallbacks="$(for i in $(seq 1 10); do jq -r '.errorMessage // empty' "/tmp/vaultscale-circuit-$i.json"; done | grep -c 'circuit breaker open' || true)"
echo "fallback_responses=$fallbacks"
if [[ "$fallbacks" == 0 ]]; then
  echo "Circuit state transition not verified; leaving benchmark unmeasured." >&2
fi
