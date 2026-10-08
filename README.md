# Water Quality Sampling Portal

A role-based web application for managing the complete lifecycle of water quality samples —
from field collection through laboratory analysis, review, and approval — delivered through a
full DevOps pipeline (Git → Jenkins CI → Selenium quality gate → Docker → Ansible).

## What it does

- **Create, view, update, and search** water quality sample records
  (by keyword, status, station, collection date).
- **Role-based status workflow:** `DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED`
  (plus `REJECTED → DRAFT` rework), with a full audit history of every change.
- **Roles:** `COLLECTOR`, `ANALYST`, `REVIEWER`, `ADMIN`
  (collector submits, analyst/reviewer advance review, reviewer approves/rejects,
  admin manages stations + everything).
- **Summary dashboard** — counts by status, by station, recent samples
  (HTML at `/dashboard`, JSON at `/api/dashboard/summary`).
- **Sampling-station management** (admin) and JSON API (`/api/samples`).

## Tech stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 (Temurin) |
| Framework | Spring Boot 3.3.5 (WAR packaging) |
| Build | Maven 3.9 |
| Database | H2 (dev, zero-setup) / PostgreSQL 16 (prod via `prod` profile) |
| UI | Thymeleaf + Spring Security extras |
| CI | Jenkins 2.x LTS (Jenkinsfile pipeline) |
| Testing | JUnit 5 + Spring Security Test + Selenium 4 (headless Chrome) |
| Containers | Docker (multi-stage) + Docker Compose |
| Config mgmt | Ansible (inventory + playbook + rollback) |

## Local development (conda env `os` for helpers)

```bash
conda activate os
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home

cd water-quality-portal
mvn clean verify        # 22 tests: unit + security + 5 Selenium journeys
mvn spring-boot:run     # embedded Tomcat + in-memory H2
```

Open `http://localhost:8080/water-quality-portal` → redirects to `/dashboard`.

Demo accounts: `admin/admin123`, `collector/collector123`,
`analyst/analyst123`, `reviewer/reviewer123`.

Health: `conda run -n os python scripts/health_check.py`
(or `curl http://localhost:8080/water-quality-portal/actuator/health`).

## Project structure

```
.
├── docs/                      # Weeks 1–15 deliverables + DEMO + TROUBLESHOOTING
│   ├── 01-problem-statement.md … 15-final-release.md
│   ├── DEMO.md                # lab demonstration script (start here for the viva)
│   └── TROUBLESHOOTING.md
├── water-quality-portal/      # Spring Boot application
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/waterquality/portal/
│       │   ├── config/        # SecurityConfig, DataInitializer, Station converter
│       │   ├── controller/    # dashboard, samples (view/form/workflow), stations, API
│       │   ├── domain/        # User, Station, Sample, StatusHistory, Role, SampleStatus
│       │   ├── repository/    # Spring Data JPA + search/count queries
│       │   ├── security/      # DB-backed UserDetailsService
│       │   └── service/       # SampleService (workflow guards + history + dashboard)
│       ├── main/resources/    # application.yml (dev H2 / prod Postgres), templates, css
│       └── test/              # SampleServiceTest, WebSecurityTest, WaterQualitySeleniumTest
├── docker/                    # Dockerfile (multi-stage) + docker-compose + README
├── jenkins/                   # Jenkinsfile + job-config.md + README
├── ansible/                   # inventory.ini + playbook.yml + rollback.yml + README
├── scripts/                   # health_check.py (conda os) + demo.sh
├── .github/                   # issue templates
└── .gitignore
```

## DevOps pipeline

```
git push → Jenkins (checkout → build → unit test → Selenium gate)
  → archive WAR → Docker build (versioned) → deploy container → Ansible provision
```

- Failed tests stop the pipeline before Docker/deploy stages (see `jenkins/Jenkinsfile`).
- Provisioning is idempotent (`changed=0` on rerun); rollback via `ansible/rollback.yml`.
- Full lab demo: follow `docs/DEMO.md`.

## Git workflow

Single branch `master`, meaningful commits (`feat/fix/docs/chore/test/ci/build`),
release tags like `v1.0.0` (annotated, on master — see `docs/15-final-release.md`).
