# Docker — Water Quality Sampling Portal

Image: versioned tag `water-quality-portal:1.0.0` (also `:latest` alias).
Stack: `docker/docker-compose.yml` (app + postgres:16-alpine).
All commands assume repo root (`/Users/ash/Desktop/devops`) unless noted.

## 1. Build

```bash
# Build versioned image from repo root
docker build -f docker/Dockerfile -t water-quality-portal:1.0.0 .

# Tag aliases (versioned + latest)
docker tag water-quality-portal:1.0.0 water-quality-portal:latest

# Verify
docker images | grep water-quality-portal
```

## 2. Run (standalone container + external/networked DB)

```bash
# Option A — full stack via compose (recommended)
docker compose -f docker/docker-compose.yml up -d --build

# Option B — app container only (needs reachable Postgres)
docker run -d --name wq-portal-app -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/waterquality \
  -e SPRING_DATASOURCE_USERNAME=wquser \
  -e SPRING_DATASOURCE_PASSWORD=wqpass \
  water-quality-portal:1.0.0
```

Port mapping: host `8080` → container `8080`.
App URL: http://localhost:8080/water-quality-portal/
Health: http://localhost:8080/water-quality-portal/actuator/health

## 3. Logs / inspect / health

```bash
docker logs -f wq-portal-app
docker logs -f wq-portal-db
docker ps --filter name=wq-portal
docker inspect wq-portal-app
docker inspect --format='{{json .State.Health}}' wq-portal-app
curl -f http://localhost:8080/water-quality-portal/actuator/health
```

## 4. Stop / restart / remove

```bash
# Compose lifecycle
docker compose -f docker/docker-compose.yml stop      # stop, keep containers
docker compose -f docker/docker-compose.yml start     # restart stopped stack
docker compose -f docker/docker-compose.yml restart app
docker compose -f docker/docker-compose.yml down      # stop + remove containers/network
docker compose -f docker/docker-compose.yml down -v  # also drop pgdata volume (DATA LOSS)

# Standalone container lifecycle
docker stop wq-portal-app
docker start wq-portal-app
docker restart wq-portal-app
docker rm -f wq-portal-app
```

## 5. Compose cheatsheet

| Task | Command |
|---|---|
| Start stack | `docker compose -f docker/docker-compose.yml up -d --build` |
| Status | `docker compose -f docker/docker-compose.yml ps` |
| Follow logs | `docker compose -f docker/docker-compose.yml logs -f` |
| Rebuild app only | `docker compose -f docker/docker-compose.yml up -d --build app` |
| Tear down | `docker compose -f docker/docker-compose.yml down` |

## 6. Troubleshooting

- `port 8080 already allocated` → `lsof -i :8080` then stop the holder, or remap `-p 8081:8080`.
- App starts before DB is ready → compose `depends_on: service_healthy` handles this; standalone `docker run` does not — start Postgres first.
- Health stays `starting/unhealthy` → `docker logs wq-portal-app` and check `SPRING_DATASOURCE_URL` / credentials.
