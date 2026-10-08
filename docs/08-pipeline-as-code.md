# Week 8 — Pipeline as Code (`jenkins/Jenkinsfile`)

**Goal:** the whole delivery path lives in one versioned Declarative pipeline
(US-5.1). Companion setup doc: `jenkins/job-config.md`.

## Stages (in order)

| # | Stage | Command / step |
|---|-------|----------------|
| 1 | Checkout | `checkout scm` |
| 2 | Build | `mvn -B clean package -DskipTests` (Temurin 21 `JAVA_HOME` preferred) |
| 3 | Unit Test | `mvn -B test` + `junit surefire-reports/*.xml` |
| 4 | Selenium Gate | `mvn -B verify -Dtest=SeleniumSuite -DfailIfNoTests=false \|\| mvn -B verify` + failsafe/surefire publish |
| 5 | Archive | `archiveArtifacts 'water-quality-portal/target/*.war'` |
| 6 | Docker Build | `docker build -f docker/Dockerfile -t water-quality-portal:${APP_VERSION} .` |
| 7 | Docker Publish | tag `:latest` alias; image stays in local daemon (push snippet commented) |
| 8 | Deploy | `docker compose -f docker/docker-compose.yml up -d --build` (fallback: `docker run wq-portal-app-$ENV_NAME`), then `curl -f .../actuator/health` |

Declarative pipelines abort on first failure, so a red Build/Unit Test/Selenium
stage means Archive/Docker/Deploy never run.

## Parameters & versioning

- `ENV_NAME` (string, default `staging`) — deploy container named `wq-portal-app-$ENV_NAME`.
- `SKIP_SELENIUM` (boolean, default `false`) — bypass gate only on browser-less agents.
- `APP_VERSION` (env) — `git describe --tags --always`, fallback `1.0.0`;
  becomes the Docker tag (`water-quality-portal:${APP_VERSION}`).
- Poll trigger `H/2 * * * *` configured in the job (not in the Jenkinsfile).

## Deploy + health check

The Deploy stage passes `ENV_NAME`/`APP_VERSION` into compose, waits ~5 s, then
fails the build unless `GET /water-quality-portal/actuator/health` returns 200.
`post` block: `always` re-archives WAR + surefire XML; `failure` prints
"deployment was NOT performed"; `success` prints the deployed version/env.

## Evidence checklist

- `docs/screenshots/jenkins-console.png` — green stages incl. Selenium gate.
- `docs/screenshots/jenkins-artifacts.png` — archived `.war`.
- `docs/screenshots/jenkins-gate-fail.png` — red run where Deploy is skipped.
- `docker images | grep water-quality-portal` shows the `${APP_VERSION}` tag.
