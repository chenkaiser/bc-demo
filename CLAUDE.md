# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & test

All Maven commands run from the **repo root** (where the parent `pom.xml` lives). The version is injected at build time via `-Drevision`; omit it to use the default `1.0.0`.

```bash
# Build everything (skip tests)
mvn package -DskipTests

# Build a single module (also compiles its parent)
mvn package -pl greet-service -am -DskipTests

# Run all tests
mvn test

# Run tests for one module
mvn test -pl api-gateway

# Run a single test class
mvn test -pl greet-service -Dtest=GreetControllerTest

# Build with a specific version (used by CI)
mvn verify -Drevision=1.2.3
```

## Running locally with Podman Compose

**One-time setup** — generate the TLS keystore and add the Keycloak hostname alias:

```bash
bash scripts/generate-certs.sh
echo "127.0.0.1  keycloak" | sudo tee -a /etc/hosts
```

**Start the stack:**

```bash
podman compose up --build
# or with a specific version baked into the images:
APP_VERSION=1.2.3 podman compose up --build
```

**Get a token and call the gateway:**

```bash
TOKEN=$(curl -s -X POST http://keycloak:8080/realms/bc-demo/protocol/openid-connect/token \
  -d "client_id=bc-demo-client&grant_type=password&username=testuser&password=password" \
  | jq -r .access_token)

curl -k -H "Authorization: Bearer $TOKEN" https://localhost:8443/greet-service/api/greet/world
curl -k -H "Authorization: Bearer $TOKEN" https://localhost:8443/hello-service/api/hello
```

Observability UIs: Grafana `http://localhost:3000` (admin/admin), Prometheus `http://localhost:9090`, Tempo `http://localhost:3200`.

## Architecture

```
Client → [HTTPS :8443] API Gateway → lb://greet-service (round-robin, 2 instances)
                                   → lb://hello-service (round-robin, 2 instances)
         Keycloak :8080  (JWT issuer)
         Eureka   :8761  (service registry)
         Redis    :6379  (rate limiter state)
```

**Request pipeline (per gateway route, in order):**

1. `RequestIdFilter` (global, `HIGHEST_PRECEDENCE`) — assigns/echoes `X-Request-ID`
2. Spring Security — validates JWT Bearer token → 401 if missing/invalid, 403 if unauthorized
3. `RequestRateLimiter` filter — Redis token bucket, 10 req/s per JWT subject, burst 20
4. `CircuitBreaker` filter — COUNT_BASED, 10-request sliding window, opens at 50% failures, stays open 30 s
5. `Retry` filter — 2 retries on 502/503/504, GET/HEAD only, exponential backoff 100 ms → 1 s

Rate limiter is first so that over-quota clients don't consume circuit breaker budget.

**Error handling — three separate layers:**

| Layer | Class | Handles |
|---|---|---|
| Security | `SecurityConfig.writeError()` | 401 (bad token), 403 (denied) |
| Gateway routing | `GlobalGatewayErrorHandler` (`@Order(-2)`) | 404 no-route, 429 rate limit, 503 circuit open, unhandled 500s |
| Backend services | `GlobalExceptionHandler` (`@RestControllerAdvice`) | Validation (400), not found (404), generic (500) |

All layers produce the same JSON shape: `{ timestamp, status, error, message, path }`. Backend services add `traceId` from Micrometer MDC.

**Versioning (CI-friendly Maven):**

The root POM uses `<version>${revision}</version>` with a default of `1.0.0`. Child POMs inherit this with `<parent><version>${revision}</version>`. The `flatten-maven-plugin` writes a `.flattened-pom.xml` during `process-resources` so installed artifacts carry a concrete version. On merge to `master`, GitHub Actions (`.github/workflows/release.yml`) reads Conventional Commits since the last tag, bumps semver (`feat` → minor, `fix`/`perf` → patch, `BREAKING CHANGE` → major), and creates a GitHub Release. `chore`/`docs`/`ci` commits produce no release.

**Multi-module Docker builds:**

All Dockerfiles use the **repo root** as build context (configured in `compose.yml`). Each Dockerfile copies all module `pom.xml` files first (layer cache for dependency download), then only its own `src/`. JAR names have no version suffix (`greet-service.jar`) so Dockerfiles never need updating on release. `APP_VERSION` build arg threads the version into `-Drevision=${APP_VERSION}` in every Maven command.

**Service discovery gotchas:**

- `lb://greet-service` in gateway routes must exactly match `spring.application.name` in the backend service.
- `EUREKA_INSTANCE_PREFER_IP_ADDRESS=true` is required when running multiple replicas in Compose; without it all instances register under the same container hostname and Eureka deduplicates them.
- The Keycloak `iss` JWT claim uses the hostname the token was minted with. Both the host machine and containers must reach Keycloak as `http://keycloak:8080` — hence the `/etc/hosts` entry.

## Key files

| Path | Purpose |
|---|---|
| `api-gateway/src/main/resources/application.yml` | All gateway config: routes, resilience, SSL, rate limits, tracing |
| `api-gateway/src/main/java/com/example/gateway/SecurityConfig.java` | JWT validation, permit list, custom 401/403 JSON |
| `api-gateway/src/main/java/com/example/gateway/GlobalGatewayErrorHandler.java` | Catch-all error → JSON for routing/infra errors |
| `keycloak/realm-import.json` | Realm `bc-demo`, client `bc-demo-client`, role `USER`, user `testuser/password` |
| `scripts/generate-certs.sh` | Generates `certs/keystore.p12` (PKCS12, self-signed, 365 days) |
| `observability/prometheus.yml` | Scrape config for all four services |
| `.github/workflows/release.yml` | Semantic-version release on push to master |
