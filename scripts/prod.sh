#!/usr/bin/env bash
# Start the production stack (HTTPS :8443, 2 replicas, observability).
# Prerequisite: bash scripts/generate-certs.sh
# Usage: bash scripts/prod.sh [up|down|logs] [extra args]
set -euo pipefail
CMD=${1:-up}; shift || true
COMPOSE_FILE=compose.yml:compose.prod.yml podman-compose "$CMD" "$@"
