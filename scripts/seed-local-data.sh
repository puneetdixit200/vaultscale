#!/bin/bash
# scripts/seed-local-data.sh
# Creates one demo user + one demo organization via the real API —
# useful for quickly having test data after a fresh docker compose up.

set -e

BASE_URL="http://localhost:8080/api/v1"

echo "Registering demo user..."
RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@vaultscale.com","password":"demo12345","fullName":"Demo User"}')

# Extract the JWT token from the JSON response using grep+sed (no jq dependency needed)
TOKEN=$(echo "$RESPONSE" | grep -o '"token":"[^"]*' | sed 's/"token":"//')

echo "Creating demo organization..."
curl -s -X POST "$BASE_URL/orgs" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Demo Org","slug":"demo-org"}'

echo ""
echo "✅ Seed complete. Login with: demo@vaultscale.com / demo12345"
