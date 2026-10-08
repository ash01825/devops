# Week 15 — Final Release

## Complete workflow

```
git push (master) → Jenkins polls (H/2 * * * *) → Checkout → Build (mvn package) →
Unit Test → Selenium Quality Gate → Archive WAR → Docker Build (${APP_VERSION}) →
Docker Publish (:latest alias) → Deploy (compose up + health 200) →
Ansible provision (/opt/wq-portal, idempotent) → rollback.yml on demand
```

Quality gate position is structural: the Selenium stage precedes every release
stage, and Declarative pipelines abort on first failure — red tests mean no image,
no deploy. See `docs/10-continuous-testing.md`, `jenkins/Jenkinsfile`.

## Architecture recap

Spring Boot 3.3.5 WAR (Java 21) → WAR on Tomcat 10.1; PostgreSQL 16 prod / H2 dev;
Thymeleaf UI; Spring Security roles `COLLECTOR/ANALYST/REVIEWER/ADMIN`;
JPA entities `User/Station/Sample/StatusHistory`; JSON via `ApiController`;
CI `jenkins/Jenkinsfile`; containers `docker/`; provisioning `ansible/`.
Details: `docs/03-architecture.md`.

## Limitations

- **Single-node** local staging only — no HA, no load balancing, no DR beyond rollback.
- **H2 default locally** — zero-setup but not prod-faithful; prod profile needs real Postgres.
- **Selenium needs Chrome** on the agent (`SKIP_SELENIUM` weakens the gate).
- **Jenkins manual install** (brew/Docker) with local daemon — no agents fleet, no secrets vault.
- Compose volume `pgdata` is wiped by `down -v`; rollback covers the app, not the data.

## Future work

GIS maps + geospatial search; sensor/IoT ingest; CSV/PDF exports + regulatory
submission; public read-only portal; multi-tenant accounts; mobile app; HA
(multi-replica, managed Postgres, backups); secrets management; monitoring/alerting.
All are explicitly out of MVP scope (`docs/01-problem-statement.md` §7).

## Viva Q&A (10 likely questions)

1. **Why WAR + external Tomcat?** Project stack requires deployable WAR on Tomcat 10.1;
   multi-stage `docker/Dockerfile` builds the WAR then ships only Tomcat + WAR.
2. **Why idempotency matters?** Safe re-runs: `changed=0` proves provisioning converges
   instead of duplicating containers/files (`state=directory/present`, `pull=missing`).
3. **How does the quality gate block deploy?** Selenium stage sits before release stages;
   Declarative abort-on-failure means red tests skip Docker/Deploy automatically.
4. **How does rollback work?** `ansible/rollback.yml -e prev_version=X` restarts
   `wq-portal-app` on the old tag and fails unless actuator health is 200.
5. **Role matrix?** DRAFT→SUBMITTED: collector/admin; SUBMITTED→UNDER_REVIEW:
   analyst/reviewer/admin; UNDER_REVIEW→APPROVED/REJECTED: reviewer/admin
   (comment required to reject); REJECTED→DRAFT: collector/admin.
6. **What is the frozen MVP scope?** Auth + CRUD/search + workflow/history + dashboard +
   stations admin + full pipeline + seed data. Sensors/GIS/exports/HA are deferred.
7. **Where does APP_VERSION come from?** `git describe --tags --always` (fallback 1.0.0);
   tags like `v1.0.0` become the Docker tag.
8. **What fixed the station binding bug?** `StringToStationConverter`
   (`Converter<String, Station>`) maps the posted station id to an entity.
9. **How is auditability guaranteed?** `SampleService` writes a `StatusHistory` row on
   every create/transition; `samples/view` shows `#historyTable`.
10. **How do you prove CI ran?** Build Changes + Polling Log, green console, Test Result
    page, archived `.war`, `docker images` tag, actuator 200 after deploy.
