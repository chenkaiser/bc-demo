#!/usr/bin/env bash
# Start infra only (no gateway) for local gateway development.
# After this: mvn spring-boot:run -f api-gateway/pom.xml -Dspring-boot.run.profiles=local
# Usage: bash scripts/infra.sh [up|down|logs] [extra args]
set -euo pipefail
CMD=${1:-up}; shift || true
COMPOSE_FILE=compose.yml:compose.infra.yml podman-compose "$CMD" "$@"
