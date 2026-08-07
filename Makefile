# Makefile
# Run "make <command>" from the project root instead of typing long commands.

.PHONY: up down test build logs clean seed

# Start the full Dockerized stack
up:
	docker compose up -d --build

# Stop everything
down:
	docker compose down

# Run backend tests
test:
	cd backend && ./mvnw test

# Rebuild images without starting containers
build:
	docker compose build

# Tail logs from all containers live
logs:
	docker compose logs -f

# Remove containers AND volumes (full reset — wipes the database too!)
clean:
	docker compose down -v

# Register a demo user + org for quick manual testing
seed:
	bash scripts/seed-local-data.sh
