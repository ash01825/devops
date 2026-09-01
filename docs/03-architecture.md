# Water Quality Sampling Portal — Requirements, Architecture & Technology Setup

**Week 3 Deliverable**

---

## 1. SRS Summary

The Water Quality Sampling Portal is a role-based web application for managing the full
lifecycle of water quality samples. Users log in, create and manage sample records, move
samples through a controlled status workflow, search records, and view a summary dashboard.
The system is delivered through a complete DevOps pipeline (Git → Jenkins CI → Selenium →
Docker → Ansible).

### Functional requirements (MVP)

| ID | Requirement |
|----|-------------|
| FR-1 | Authenticate users with username/password and enforce four roles |
| FR-2 | Create, view, update, and search sample records |
| FR-3 | Enforce role-based status workflow transitions |
| FR-4 | Record a full audit history of every status change |
| FR-5 | Provide a summary dashboard (counts by status/station/date) |
| FR-6 | Manage sampling stations (admin) |
| FR-7 | Deliver through automated build → test → deploy pipeline |

### Non-functional requirements

| ID | Requirement |
|----|-------------|
| NFR-1 | Passwords hashed, never plaintext |
| NFR-2 | Role-guarded endpoints and UI actions |
| NFR-3 | Repeatable (idempotent) provisioning |
| NFR-4 | Rollback to previous release within minutes |
| NFR-5 | Failed tests block deployment |
| NFR-6 | Runs on local VMs + Docker Compose (Apple Silicon safe) |

---

## 2. Use-Case Diagram

```
                    ┌──────────────────────────────┐
                    │      Water Quality Portal     │
                    └──────────────────────────────┘
                                   │
        ┌──────────────────────────┼──────────────────────────┐
        │                          │                          │
   ┌────┴────┐                ┌────┴────┐                ┌────┴────┐
   │ COLLECTOR│               │ ANALYST │                │ REVIEWER│
   └────┬────┘                └────┬────┘                └────┬────┘
        │                          │                          │
        │ create sample            │ update measurements       │ approve/reject
        │ submit sample            │ search samples            │ review samples
        │                          │                          │
        └──────────────────────────┼──────────────────────────┘
                                   │
                              ┌────┴────┐
                              │  ADMIN  │
                              └─────────┘
                              manage users, stations, all roles
```

---

## 3. Architecture Diagram

```
┌────────────────────────  Dev Workstation  ────────────────────────┐
│  Spring Boot (WAR)   JPA entities   Repositories   Controllers    │
│  Thymeleaf UI   Maven (3.9)   Java 21   JUnit 5   Selenium        │
└──────────────────────────────┬─────────────────────────────────────┘
                               │ git commit / push (you run)
                               ▼
┌──────────────────────────  Jenkins (Docker)  ─────────────────────┐
│  Checkout → mvn clean package → Selenium gate → Docker build       │
│  (artifact archive, test reports, versioned image publish)         │
└──────────────────────────────┬─────────────────────────────────────┘
                               │ deploy
                               ▼
┌──────────────────────────  Target Node  ──────────────────────────┐
│  PostgreSQL 16  ──  Tomcat 10.1 (WAR)  ──  App container           │
│  (provisioned + configured by Ansible)                             │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 4. Data Model

| Entity | Fields | Relationships |
|--------|--------|---------------|
| **User** | id, username, password, fullName, email, role, enabled | — |
| **Station** | id, code, name, waterBody, latitude, longitude | 1 → many Sample |
| **Sample** | id, sampleId, station, collectedAt, collector, ph, temperature, dissolvedOxygen, turbidity, conductivity, status, notes, createdAt, updatedAt | many → 1 Station; 1 → many StatusHistory |
| **StatusHistory** | id, sample, fromStatus, toStatus, changedBy, changedAt, comment | many → 1 Sample |

**Enums:** `Role` (COLLECTOR, ANALYST, REVIEWER, ADMIN) · `SampleStatus` (DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED)

---

## 5. API List

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/auth/login` | Authenticate |
| GET | `/api/samples` | List + search (`search`, `status`, `station`, `page`) |
| POST | `/api/samples` | Create sample |
| GET | `/api/samples/{id}` | View sample detail |
| PUT | `/api/samples/{id}` | Update sample |
| POST | `/api/samples/{id}/status` | Workflow transition |
| GET | `/api/dashboard/summary` | Summary dashboard |

---

## 6. Technology Setup

| Concern | Choice | Version |
|---------|--------|---------|
| Language | Java (Temurin) | 21 |
| Framework | Spring Boot | 3.3.5 |
| Build | Maven | 3.9.16 |
| Packaging | WAR (external Tomcat) | — |
| Database | PostgreSQL (prod) / H2 (dev) | 16 / 2.x |
| UI | Thymeleaf | via Boot |
| Auth | Spring Security | via Boot |
| Test | JUnit 5, Selenium | 5 / 4 |
| Deploy | Tomcat | 10.1 |
| CI | Jenkins | 2.x LTS |
| Containers | Docker + Compose | 28 / 2.40 |
| Config mgmt | Ansible | latest |

### Local dev setup

```bash
# 1. Build and test
cd water-quality-portal
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn clean verify

# 2. Run with embedded Tomcat + in-memory H2
mvn spring-boot:run

# 3. Open (context-path is /water-quality-portal)
open http://localhost:8080/water-quality-portal
```

> Dev profile uses H2 (in-memory) for zero-setup local runs. Prod profile will use
> PostgreSQL via environment variables, introduced in the Docker/deploy weeks.
