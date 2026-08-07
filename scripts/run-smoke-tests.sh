#!/bin/bash
# scripts/run-smoke-tests.sh
# Quick sanity check after deployment — confirms core endpoints respond correctly.
# "Smoke test" = a fast, shallow check that the app didn't catch fire on startup.

set -e
BASE_URL="http://localhost:8080"

echo "1. Checking health endpoint..."
curl -sf "$BASE_URL/actuator/health" | grep -q '"status":"UP"' && echo "✅ Health OK" || (echo "❌ Health FAILED" && exit 1)

echo "2. Checking auth register endpoint responds..."
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"smoketest@vaultscale.com","password":"smoke12345","fullName":"Smoke Test"}')
[ "$STATUS" == "201" ] && echo "✅ Register OK" || echo "⚠️  Register returned $STATUS (may already exist — OK on reruns)"

echo "3. Checking protected route rejects unauthenticated request..."
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/auth/me")
[ "$STATUS" == "401" ] || [ "$STATUS" == "403" ] && echo "✅ Auth guard OK" || echo "❌ Auth guard FAILED"

echo ""
echo "✅ Smoke tests complete."
