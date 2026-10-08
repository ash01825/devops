# Week 11 — Docker Lifecycle

**Sources:** `docker/Dockerfile`, `docker/docker-compose.yml`. Commands assume repo root.

## Multi-stage Dockerfile

- **Stage 1 (builder)** `maven:3.9-eclipse-temurin-21`: copies `pom.xml` + `src`,
  runs `mvn -B package -DskipTests` (tests already ran in CI).
- **Stage 2 (runtime)** `tomcat:10.1-jdk21-temurin`: clears `webapps/*`, copies the
  WAR to `webapps/water-quality-portal.war` (context path `/water-quality-portal`).
- Env defaults: `SPRING_PROFILES_ACTIVE=prod`, `DB_URL/DB_USER/DB_PASS`
  (postgres `db:5432/waterquality`, `wquser/wqpass`); `EXPOSE 8080`.
- `HEALTHCHECK` polls `/water-quality-portal/actuator/health` (30 s interval,
  60 s start period).

## Image build / tag / run / logs / stop / restart / remove / inspect

```bash
docker build -f docker/Dockerfile -t water-quality-portal:1.0.0 .
docker tag water-quality-portal:1.0.0 water-quality-portal:latest
docker images | grep water-quality-portal          # verify tags
docker run -d --name wq-portal-app -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/waterquality \
  -e SPRING_DATASOURCE_USERNAME=wquser -e SPRING_DATASOURCE_PASSWORD=wqpass \
  water-quality-portal:1.0.0
docker logs -f wq-portal-app
docker inspect wq-portal-app
docker inspect --format='{{json .State.Health}}' wq-portal-app
docker stop wq-portal-app && docker start wq-portal-app
docker restart wq-portal-app
docker rm -f wq-portal-app
```

## Docker Compose (postgres:16-alpine + app)

- `db`: `postgres:16-alpine`, volume `pgdata`, port `${DB_HOST_PORT:-5433}:5432` (host 5433, container 5432),
  healthcheck `pg_isready -U wquser -d waterquality`.
- `app`: builds from `..`/`docker/Dockerfile`, image `water-quality-portal:1.0.0`,
  port `8080:8080`, `depends_on: db service_healthy`, datasource env pointing at `db`.
- Port mapping: host `8080` → container `8080`.

```bash
docker compose -f docker/docker-compose.yml up -d --build
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs -f
docker compose -f docker/docker-compose.yml down   # add -v to drop pgdata (DATA LOSS)
```

## Health endpoint check

```bash
curl -f http://localhost:8080/water-quality-portal/actuator/health
```

Expect `{"status":"UP"}`. App URL: `http://localhost:8080/water-quality-portal/`.
