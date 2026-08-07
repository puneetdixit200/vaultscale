# Quantitative impact checks

VaultScale has two different measurable outcomes. Keep them separate:

| Track | Measures | Output |
| --- | --- | --- |
| Performance | API latency, throughput, HTTP failures, and authenticated-route failures under a defined load | k6 JSON summary |
| Product and security outcomes | Persisted users, workspaces, collections, saved endpoints, execution success, response-time percentiles, blocked unsafe URLs, and audit events | PostgreSQL JSON snapshot |

## Run the product-impact report

Start the database stack, then run:

```bash
./scripts/generate-impact-report.sh
```

The report is written to `artifacts/impact/` and is ignored by Git. It contains counts only, not user email addresses, tokens, request bodies, URLs, or response bodies.

## Run both checks

Install [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/), start the backend, then run:

```bash
VUS=5 DURATION=30s ./scripts/run-impact-tests.sh
```

`BASE_URL` defaults to `http://localhost:8080`. Set it explicitly for a non-local deployment, for example `BASE_URL=https://vaultscale.example.com`.

The k6 test creates one real test account per run and calls only `GET /api/v1/auth/me` and `GET /api/v1/orgs`. Do not aim it at production without approval: it creates persisted data and exercises real capacity.

## Reading the numbers

- Treat p50/p95 latency and HTTP failure rate as technical performance evidence for the exact environment and load used.
- Treat database totals as product-usage evidence only for data retained in this instance.
- `ssrf_or_invalid_url_blocks` is a best-effort classification of stored request-history errors; it is not a count of all attack attempts.
- Never describe these measurements as user growth, revenue, uptime, or prevented incidents unless independent data proves those claims.
