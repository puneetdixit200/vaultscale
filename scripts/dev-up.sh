#!/bin/bash
# scripts/dev-down.sh
# Stops all containers without deleting data (volumes stay intact)

set -e
docker compose down
echo "✅ All containers stopped. Data preserved in Docker volumes."
