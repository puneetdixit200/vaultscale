#!/usr/bin/env bash
set -euo pipefail

# Measures JSON audit events through Kafka -> standalone audit-service -> audit-postgres.
count="${EVENTS:-1000}"
topic="${KAFKA_TOPIC:-vaultscale.audit.events}"
group="${KAFKA_GROUP:-vaultscale-audit-benchmark-$(date +%s)}"
kafka_container="${KAFKA_CONTAINER:-vaultscale_kafka}"
db_container="${AUDIT_DB_CONTAINER:-vaultscale_audit_postgres}"
action="BENCHMARK_AUDIT_$(date +%s%N)"
user_id="00000000-0000-0000-0000-000000000321"

if [[ "${RESET_OFFSETS:-false}" == "true" ]]; then
  docker exec "$kafka_container" kafka-consumer-groups --bootstrap-server localhost:9092 --group "$group" --topic "$topic" --reset-offsets --to-latest --execute >/dev/null 2>&1 || true
fi
payload() { for _ in $(seq 1 "$count"); do printf '{"action":"%s","organizationId":null,"userId":"%s","metadata":{"benchmark":true}}\n' "$action" "$user_id"; done; }
start_ns="$(date +%s%N)"
payload | docker exec -i "$kafka_container" kafka-console-producer --bootstrap-server localhost:9092 --topic "$topic" >/dev/null
end_ns="$(date +%s%N)"
consumed=0
for _ in $(seq 1 120); do
  consumed="$(docker exec "$db_container" psql -U audituser -d audit_db -Atc "select count(*) from audit_logs where action='$action';" | tr -d '[:space:]')"
  if [[ "$consumed" == "$count" ]]; then
    break
  fi
  sleep 0.25
done
lag="$(docker exec "$kafka_container" kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group "$group" 2>/dev/null | awk -v topic="$topic" '$1 == topic && NF >= 5 {sum += $5} END {print sum+0}')"
python3 - "$count" "$consumed" "$start_ns" "$end_ns" "$lag" <<'PY'
import sys
count, consumed = int(sys.argv[1]), int(sys.argv[2])
duration = (int(sys.argv[4]) - int(sys.argv[3])) / 1e9
print(f"events_produced={count}")
print(f"events_consumed={consumed}")
print(f"producer_seconds={duration:.3f}")
print(f"producer_events_per_sec={count/duration:.2f}")
print(f"consumer_events_per_sec={consumed/duration:.2f}")
print(f"failed_events={count-consumed}")
print(f"final_lag={sys.argv[5]}")
PY
