#!/usr/bin/env bash
set -euo pipefail

# Controlled local benchmark for the real GET /api/v1/orgs Redis cache.
base_url="${BASE_URL:-http://127.0.0.1:8080}"
redis_container="${REDIS_CONTAINER:-vaultscale_redis}"
samples="${SAMPLES:-30}"
suffix="$(date +%s%N)"
registration="$(curl -fsS -X POST "$base_url/api/v1/auth/register" -H 'Content-Type: application/json' --data-binary "{\"fullName\":\"Redis Benchmark\",\"email\":\"redis-$suffix@example.test\",\"password\":\"ImpactTest123!\"}")"
token="$(jq -r .token <<<"$registration")"
email="redis-$suffix@example.test"
user_id="$(docker exec "${POSTGRES_CONTAINER:-vaultscale_postgres}" psql -U vaultscale -d vaultscale -Atc "select id from users where email='$email';" | tr -d '[:space:]')"
[[ -n "$token" && "$token" != null ]] || { echo "registration did not return a token" >&2; exit 1; }

measure_ms() {
  curl -fsS -o /dev/null -w '%{time_total}\n' -H "Authorization: Bearer $token" "$base_url/api/v1/orgs" | awk '{printf "%.3f", $1 * 1000}'
}

clear_relevant_key() {
  [[ -z "$user_id" ]] && return 0
  docker exec "$redis_container" sh -c "redis-cli --scan --pattern 'myOrgs::*' | while IFS= read -r key; do case \"\$key\" in *'$user_id') redis-cli DEL \"\$key\" >/dev/null;; esac; done"
}

metric_count() {
  curl -fsS -H "Authorization: Bearer $token" "$base_url/actuator/metrics/vaultscale.cache.misses" \
    | jq -r '.measurements[] | select(.statistic == "COUNT") | .value'
}

misses_before="$(metric_count)"
miss_values=()
for _ in $(seq 1 3); do clear_relevant_key; miss_values+=("$(measure_ms)"); done
hit_values=()
for _ in $(seq 1 "$samples"); do hit_values+=("$(measure_ms)"); done
misses_after="$(metric_count)"

python3 - "$misses_before" "$misses_after" "${miss_values[*]}" "${hit_values[*]}" <<'PY'
import statistics, sys
before, after = float(sys.argv[1]), float(sys.argv[2])
miss = [float(x) for x in sys.argv[3].split()]
hit = [float(x) for x in sys.argv[4].split()]
uncached, cached = statistics.mean(miss), statistics.mean(hit)
request_count = len(miss) + len(hit)
observed_misses = int(after - before)
observed_hits = max(0, request_count - observed_misses)
percentile = lambda values, p: sorted(values)[max(0, int(len(values) * p) - 1)]
print(f"miss_samples={len(miss)}")
print(f"miss_avg_ms={uncached:.3f}")
print(f"miss_p50_ms={statistics.median(miss):.3f}")
print(f"miss_p95_ms={percentile(miss, .95):.3f}")
print(f"hit_samples={len(hit)}")
print(f"hit_avg_ms={cached:.3f}")
print(f"hit_p50_ms={statistics.median(hit):.3f}")
print(f"hit_p95_ms={percentile(hit, .95):.3f}")
print(f"latency_reduction_pct={((uncached-cached)/uncached*100):.2f}")
print(f"cache_misses_observed={observed_misses}")
print(f"controlled_hit_rate_pct={(observed_hits/request_count*100):.2f}")
PY
