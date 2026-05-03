#!/usr/bin/env bash
# Start the dev stack (HTTP gateway on :8888, no TLS, no observability).
# Usage: bash scripts/dev.sh [up|down|logs] [extra args]
set -euo pipefail
CMD=${1:-up}; shift || true
COMPOSE_FILE=compose.yml:compose.dev.yml podman-compose "$CMD" "$@"
