# VaultScale Local Performance & Reliability Benchmarks

All results below are actual local runs, not production capacity claims.

## Benchmark Environment

| Item | Value |
|---|---|
| Repository commit tested | `51432b8` baseline; benchmark instrumentation changes are uncommitted while measured |
| OS | Arch Linux, kernel `7.1.3-arch2-2`, x86_64 |
| CPU | AMD Ryzen 5 5500U with Radeon Graphics, 12 logical CPUs |
| RAM | 14.9 GiB |
| Docker Engine | 29.6.2 |
| Java | OpenJDK 26.0.1 (Compose services use Java 21 runtime images) |
| PostgreSQL | 16.14 |
| Redis | 7.4.9 |
| Kafka | Confluent Platform `7.6.0` image |
| k6 | 2.1.0 |

The stack was health-checked before testing: backend, PostgreSQL, Redis, Kafka,
ZooKeeper, and audit PostgreSQL were healthy. The standalone audit consumer was
run from the locally built audit-service JAR for the Kafka tests.

## HTTP Load Testing

The k6 scenario exercised authenticated `GET /api/v1/auth/me` and
`GET /api/v1/orgs`, with one-second pacing per VU. Existing verified rows are
preserved exactly.

| VUs | Duration | Requests | Throughput | P50 | P95 | P99 | Errors |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 20 | 30 s | 1,201 | 39.00 req/s | — | 27.25 ms | — | 0.00% |
| 50 | 30 s | 3,001 | 98.20 req/s | — | 12.47 ms | — | 0.00% |
| 100 | 30 s | 6,001 | 193.00 req/s | 4.50 ms | 29.21 ms | — | 0.00% |
| 200 | 30 s | 12,001 | 387.66 req/s | 2.95 ms | 22.51 ms | — | 0.00% |

The 200-VU run was the highest tested level and remained below the configured
1% error and 1,000 ms P95 thresholds. P99 was not emitted by the current k6
summary export and is intentionally left unmeasured.

## Redis Cache

The real cached endpoint is `GET /api/v1/orgs`, backed by
`OrganizationService.getMyOrganizations` with `@Cacheable("myOrgs")`. A small
Micrometer counter records calls that reach the cacheable method; a cache hit
does not invoke that method. Each run used three cache-miss samples and 30
warmed-cache samples for one disposable benchmark user.

| Metric | Measured value |
|---|---:|
| Runs | 3 |
| Per-run miss average | 25.152 ms, 16.442 ms, 14.559 ms |
| Per-run hit average | 17.816 ms, 10.964 ms, 11.794 ms |
| Three-run average miss / hit | 18.718 / 13.525 ms |
| Three-run average latency reduction | 27.74% |
| Observed misses per run | 3 |
| Controlled local hit rate per run | 90.91% (30 hits / 33 requests) |

This is a synthetic local hit rate, not production traffic.

## Kafka Throughput

Using the real `vaultscale.audit.events` topic and standalone audit-service,
500 uniquely tagged JSON events were produced and verified in audit PostgreSQL.

| Events | Produced | Persisted | Producer rate | Consumer rate | Failures | Final lag |
|---:|---:|---:|---:|---:|---:|---:|
| 500 | 500 | 500 | 166.50 events/s | 166.50 events/s | 0 | 0 |

The console-producer startup is included in the measured wall-clock producer
duration, so this is an end-to-end local benchmark rather than a raw broker
maximum.

## Kafka Consumer Lag

With the audit consumer stopped, a controlled burst of 500 events advanced the
topic while the consumer group remained at its prior committed offset. Kafka's
consumer-group tooling reported a peak lag of exactly 500. After restarting the
consumer, all 500 records were persisted and final lag returned to 0. A restart
and drain timing run recorded 26.970 s from container restart to all 100 queued
records persisted; this includes application startup, Kafka group assignment,
and database writes.

## Kafka Failure Recovery

The earlier verified single-event consumer restart test remains **17.614 s**.
It stopped the standalone consumer, published one backend `ORG_CREATED` event,
restarted the consumer, and confirmed persistence in the separate audit DB.

## Circuit Breaker

This area remains **unmeasured**. A controlled probe against a deterministic
invalid DNS target produced ten HTTP 200 fallback-shaped responses with
0–79 ms client timings, but the response body retained the original DNS error;
the configured fallback message and Resilience4j actuator metrics were not
observed. Therefore no CLOSED→OPEN, HALF_OPEN recovery, or fail-fast
improvement number is claimed. The existing Resilience4j integration needs a
separate correctness fix before it can support a defensible benchmark.

## PostgreSQL Indexing

A temporary 200,000-row table was created and removed with the psql session.
The comparison query selected one row by `endpoint_id` using `EXPLAIN
(ANALYZE, BUFFERS, FORMAT JSON)`.

| Plan | Execution time | Evidence |
|---|---:|---|
| Forced sequential scan | 31.525–38.609 ms (average 33.991 ms) | `Seq Scan`, 199,999 rows removed by filter |
| Indexed plan | 0.065–0.093 ms (average 0.077 ms) | `Bitmap Heap Scan` + `Bitmap Index Scan` |

The three-run average execution-time reduction was **99.77%** over 200,000
rows. No application index was dropped or changed.

## Container Resource Usage

The prior idle snapshot is preserved:

| Scenario | Backend | Audit service | Redis | Kafka | PostgreSQL |
|---|---:|---:|---:|---:|---:|
| Earlier idle snapshot | 388.3 MiB | 314 MiB | 3.934 MiB | 209 MiB | 36.27 MiB |
| During 100-VU sample | 420.2 MiB / 55.74% CPU | 317.7 MiB / 0.33% CPU | 6.328 MiB / 6.76% CPU | 235.2 MiB / 71.77% CPU | 44.33 MiB / 4.95% CPU |

The load snapshot is a single Docker `stats` sample, not a peak or capacity
measurement.

## Product / Security Snapshot

The existing local database report recorded 24 registered users, 15
organizations, 11 collections, 13 saved endpoints, 13 runs, a 53.85% run
success rate, P50 32.00 ms, P95 517.40 ms, and 3 best-effort SSRF/invalid-URL
blocks. These are retained local test-data counts only.

## Methodology / Limitations

- Results were measured on one local laptop and are not production benchmarks.
- Warmed-cache and cache-miss samples are controlled synthetic traffic.
- Kafka rates include the chosen producer tool's startup and serialization path.
- Circuit-breaker numbers are omitted because the implementation did not expose verifiable state transitions.
- P99 was not available from the current k6 summary output.
- Environment, data volume, JVM state, Docker resource contention, and network conditions affect every value.

## Resume-Safe Metrics

- Load-tested VaultScale at **200 concurrent users**, sustaining **387.66 req/s** at **22.51 ms P95** with **0% request errors**.
- Reduced repeated organization-list latency by **27.74%** across three Redis cache runs and measured a **90.91%** controlled hit rate.
- Persisted **500/500 Kafka audit events** at **166.50 events/s** with **0 failures**.
- Drained a measured Kafka consumer lag of **500 records** back to **0** after consumer restart.
- Reduced a temporary 200,000-row PostgreSQL query from **33.991 ms** average sequential scan to **0.077 ms** average bitmap-index plan (**99.77%** faster).

## Reproduction

```bash
qa/impact/redis-benchmark.sh
EVENTS=500 KAFKA_GROUP=vaultscale-audit-benchmark-run qa/impact/kafka-benchmark.sh
docker exec -i vaultscale_postgres psql -U vaultscale -d vaultscale -f - < qa/impact/postgres-index-benchmark.sql
./scripts/generate-impact-report.sh
```
