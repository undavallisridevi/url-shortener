# URL Shortener

Spring Boot 3.5 / Java 21 URL shortening service with PostgreSQL persistence, Redis cache-aside redirects, JWT authentication, Flyway migrations, and Docker Compose.

## Architecture notes

- URL IDs come from PostgreSQL sequence `url_id_seq` before the single URL insert and are Base62 encoded.
- Redis keys use `url:{shortCode}`. A short-lived `lock:url:{shortCode}` lock with token-safe Lua release reduces cache stampedes.
- PostgreSQL remains the source of truth. Soft-deleted aliases are retained and cannot be reused.
- Kafka producer/consumer and the Redis sliding-window rate limiter are contracts only, per the current implementation scope. Their event and key designs follow the project specification but are not wired into requests yet.
- The future rate limiter contract reserves `rate:ip:{ipAddress}` and `rate:user:{userId}` keys for a 100-request/60-second Redis ZSET Lua implementation.
- Kafka runs in modern KRaft mode in Compose, so a ZooKeeper container is intentionally unnecessary.
- Repository integration coverage uses Testcontainers and automatically skips only when Docker is unavailable. GitHub Actions runs the Java 21 Maven verification pipeline.

## Run

Set a production-strength JWT secret and start the stack:

```powershell
$env:JWT_SECRET = "replace-with-at-least-32-random-bytes"
docker compose up --build
```

The API listens on `http://localhost:8080`. Register or log in under `/api/v1/auth`, then pass `Authorization: Bearer <token>` to URL creation, statistics, and deletion endpoints.

Create request example:

```json
{"url":"https://amazon.com","customAlias":null,"expiryDays":30}
```
