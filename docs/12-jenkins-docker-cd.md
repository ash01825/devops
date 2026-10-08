# Week 12 — Jenkins–Docker CD (Commit to Container)

**Goal (US-5.3, part 1):** every green build produces a versioned image and
auto-deploys a fresh container. Pipeline: `jenkins/Jenkinsfile` stages
Docker Build → Docker Publish → Deploy.

## Commit-to-container flow

```
git push → Jenkins polls (H/2 * * * *) → Checkout → Build → Unit Test →
Selenium Gate → Archive WAR → Docker Build → Docker Publish → Deploy (compose up) →
curl -f .../actuator/health
```

Any red stage aborts the run — Deploy is reached only on fully green builds.

## Versioned image from APP_VERSION

- `APP_VERSION = git describe --tags --always` (fallback `1.0.0`), so image tag
  tracks the release tag (`v1.0.0` → `water-quality-portal:v1.0.0`-style tag).
- Docker Build: `docker build -f docker/Dockerfile -t water-quality-portal:${APP_VERSION} .`
- Docker Publish: `docker tag ... :latest` alias; image stays in the local daemon
  (registry `withRegistry` push snippet is commented for offline use).

## Local registry evidence

```bash
docker images | grep water-quality-portal
# water-quality-portal   1.0.0 / <APP_VERSION>   ...   (plus :latest alias)
```

Screenshot this plus the Jenkins **Console Output** (Deploy stage + health-check
`curl` 200) for the report.

## Auto-deploy fresh container after green tests

- Deploy passes `ENV_NAME`/`APP_VERSION` into
  `docker compose -f docker/docker-compose.yml up -d --build`, recreating the app
  container from the just-built image.
- Fallback (no compose plugin): `docker run --name wq-portal-app-$ENV_NAME`.
- Health gate: `curl -f http://localhost:8080/water-quality-portal/actuator/health`
  fails the build unless the fresh container is UP.

## ENV_NAME parameterization

- `ENV_NAME` (default `staging`): container named `wq-portal-app-$ENV_NAME`,
  compose project gets `ENV_NAME="$ENV_NAME"`.
- Run prod-like deploy without editing the Jenkinsfile:
  Build with Parameters → `ENV_NAME=prod`.
- Same image, different name/env per target — reproducible promotion path.
