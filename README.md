# Water Quality Sampling Portal

A role-based web application for managing the complete lifecycle of water quality samples —
from field collection through laboratory analysis, review, and approval — delivered through a
full DevOps pipeline.

## What it does

- **Create, view, update, and search** water quality sample records.
- **Role-based status workflow:** `DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED`.
- **Roles:** `COLLECTOR`, `ANALYST`, `REVIEWER`, `ADMIN`.
- **Summary dashboard** of samples by status, station, and date.
- **Full audit history** of every status change.

## Tech stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 (Temurin) |
| Framework | Spring Boot 3.3.5 (WAR packaging) |
| Build | Maven 3.9 |
| Database | PostgreSQL 16 (prod) / H2 (dev) |
| UI | Thymeleaf |
| Deploy | External Tomcat 10.1 |
| CI | Jenkins 2.x LTS |
| Testing | JUnit 5 + Selenium 4 |
| Containers | Docker + Docker Compose |
| Config mgmt | Ansible |

## Local development

```bash
cd water-quality-portal
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Build and test
mvn clean verify

# Run with embedded Tomcat + in-memory H2
mvn spring-boot:run
```

The app runs at `http://localhost:8080/water-quality-portal`.

## Project structure

```
.
├── docs/                      # Problem statement, architecture, workflow docs
├── water-quality-portal/       # Spring Boot application
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/waterquality/portal/
│       │   ├── config/        # Spring Security config
│       │   ├── domain/        # JPA entities + enums
│       │   └── repository/    # Spring Data repositories
│       ├── main/resources/    # application.yml, templates
│       └── test/              # unit + integration tests
├── docker/                    # Dockerfile + docker-compose (Week 11+)
├── jenkins/                   # Jenkinsfile + job config (Week 7+)
├── ansible/                   # inventory + playbooks (Week 13+)
├── .github/                   # issue templates
└── .gitignore
```

## DevOps pipeline

```
git push → Jenkins CI → Maven build → Selenium quality gate → Docker build → deploy → Ansible provision
```

See `docs/02-agile-devops-workflow.md` for the lifecycle diagram and Definition of Done.

## Git workflow

See `docs/04-git-branching-policy.md` (single branch, direct commits, tags).
