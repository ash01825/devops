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

- `APP_VERSION = git describe --tags --abbrev=0` (fallback `1.0.0`, `v` prefix
  stripped), so the image tag tracks the release tag
  (`v1.0.0` → `water-quality-portal:1.0.0`).
- Docker Build: `docker build -f docker/Dockerfile -t water-quality-portal:${APP_VERSION} .`
- Compose deploys that exact tag: `image: water-quality-portal:${APP_VERSION:-1.0.0}`
  (`docker/docker-compose.yml`), so "deploy version X" really runs version X.
- Docker Publish: `docker tag ... :latest` alias; image stays in the local daemon
  (registry `withRegistry` push snippet is commented for offline use).

## Local registry evidence

```bash
docker images | grep water-quality-portal
# water-quality-portal   1.0.0 / <APP_VERSION>   ...   (plus :latest alias)
docker inspect water-quality-portal:1.0.0 --format='digest: {{.Id}} created: {{.Created}}'
```

> No external registry is used in this lab setup: "published" means the
> versioned image is built, tagged, and present in the daemon (see
> `docs/evidence/docker-lifecycle.log`), ready for `docker push` via the
> commented `withRegistry` block when a registry is available. Capture the
> `docker images` output plus the Jenkins **Console Output** (Deploy stage +
> health-check `curl` 200) for the report.

## Auto-deploy fresh container after green tests

- Deploy passes `ENV_NAME`/`APP_VERSION` into
  `docker compose -f docker/docker-compose.yml up -d --build`, recreating the app
  container from the just-built image.
- Fallback (no compose plugin): `docker run --name wq-portal-app-$ENV_NAME`.
- Health gate: `curl -f http://localhost:8080/water-quality-portal/actuator/health`
  fails the build unless the fresh container is UP.

## ENV_NAME parameterization

- `ENV_NAME` (default `staging`): labels the run and names the fallback
  `docker run` container (`wq-portal-app-$ENV_NAME`). The compose deployment
  uses the pinned project `wq-portal` (so manual, Jenkins, and Ansible runs
  share state instead of colliding).
- Run prod-like deploy without editing the Jenkinsfile:
  Build with Parameters → `ENV_NAME=prod`.
- Same image, explicit version per deploy — reproducible promotion path.
