# Water Quality Sampling Portal — Problem Statement & MVP Scope

**Week 1 Deliverable**
**Project:** DevOps Pipeline for a Water Quality Sampling Portal
**Status:** Draft for stakeholder sign-off

---

## 1. Problem Statement

Water quality sampling is still managed through a mix of paper logbooks, spreadsheets, and
email threads. Field collectors record physical measurements and sample metadata in the
field, laboratory analysts record test results in separate tools, and reviewers approve or
reject samples by forwarding documents back and forth.

This fragmented, manual workflow causes three persistent problems:

1. **No single source of truth.** A sample's lifecycle — collected, analyzed, reviewed,
   approved — lives across disconnected files, so the current state of any sample is hard
   to determine without chasing people.
2. **Lost or duplicated records.** Re-keying sample data from paper into spreadsheets leads
   to transcription errors, missing samples, and duplicated IDs.
3. **Slow, opaque status tracking.** There is no role-based visibility into who must act on
   a sample next, so bottlenecks are discovered late and approvals take longer than needed.

The **Water Quality Sampling Portal** replaces these manual artifacts with a single,
role-based web application where a sample record is created once and moves through a
controlled status workflow (`DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED`),
with search, audit history, and a summary dashboard.

---

## 2. Target Users

| Persona | Role | Primary need |
|---------|------|--------------|
| Field Collector | `COLLECTOR` | Record new samples quickly and accurately at the collection site |
| Laboratory Analyst | `ANALYST` | Enter and edit measurement results for submitted samples |
| Quality Reviewer | `REVIEWER` | Review samples and approve or reject them with a comment |
| System Administrator | `ADMIN` | Manage users, sampling stations, and system configuration |

---

## 3. Stakeholders

| Stakeholder | Interest | Success measure |
|-------------|----------|-----------------|
| Environmental/quality team | Accurate, auditable water quality data | Fewer data-entry errors; complete audit trail |
| Field & lab staff | Less manual, duplicated work | Faster record creation; no re-keying |
| Reviewers | Clear queue of work | Know exactly what to approve/reject next |
| Operations/IT | Reproducible, reliable delivery | Automated build → test → deploy pipeline |
| Project sponsor | Measurable, shippable MVP in 15 weeks | All MVP scope met by week 15 |

---

## 4. Objectives

1. Provide a centralized portal to **create, view, update, and search** water quality sample
   records.
2. Enforce a **role-based status workflow** so each role can only perform its permitted
   transitions, with full history.
3. Provide a **summary dashboard** of samples by status, station, and date.
4. Deliver the application through a **complete DevOps pipeline**: version control →
   continuous integration → automated testing → containerization → continuous deployment →
   configuration management.

---

## 5. Constraints

- **Timeline:** MVP must be scoped small enough to complete within a 15-week delivery plan.
- **Stack:** Java + Spring Boot, Maven, PostgreSQL, Tomcat, Jenkins, Selenium, Docker,
  Ansible (per project requirements).
- **Environment:** Local VMs + Docker Compose for a reproducible, low-cost target (Apple
  Silicon safe).
- **Security:** Role-based access control is required; passwords must not be stored in
  plaintext.
- **Auditability:** Every status change must be recorded with who, when, and why.
- **Reliability:** Deployments must be repeatable (idempotent) and rollback must be
  demonstrable.

---

## 6. Measurable Success Criteria

| # | Criterion | How measured |
|---|-----------|--------------|
| SC-1 | A user can create a sample in under 2 minutes | Manual click-through / Selenium timing |
| SC-2 | 100% of sample records are searchable | Selenium search test passes |
| SC-3 | Every status transition is role-guarded | Selenium permission test passes |
| SC-4 | 100% of status changes are in the history log | Unit test + UI verification |
| SC-5 | Dashboard shows correct counts by status/station | Selenium dashboard test passes |
| SC-6 | CI build succeeds on every commit to `develop` | Jenkins build log SUCCESS |
| SC-7 | Failed tests block deployment | Pipeline halts before deploy stage |
| SC-8 | App deploys from a versioned Docker image | End-to-end commit-to-container demo |
| SC-9 | Ansible rerun reports `changed=0` (idempotent) | Second playbook run output |
| SC-10 | Rollback to previous stable release succeeds | Health check after rollback returns 200 |

---

## 7. Frozen MVP Scope

### In scope

- **Authentication:** login/logout; four roles (`COLLECTOR`, `ANALYST`, `REVIEWER`, `ADMIN`).
- **Sample records:** create, view, update, search (by sample ID, status, station, date).
- **Status workflow:** `DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED`, with
  role-based transition permissions and an audit history.
- **Sampling stations:** basic station management (admin).
- **Dashboard:** summary counts by status, station, and collection date.
- **DevOps pipeline:** Git/GitHub, Jenkins CI, Selenium quality gate, Docker, Jenkins–Docker
  CD, Ansible provisioning, rollback demo.
- **Seed data:** a small dataset of stations, users, and sample records for demos and tests.

### Out of scope (deferred)

- Real-time sensor integration / IoT telemetry.
- GIS map visualization and geospatial search.
- Public-facing anonymous data portal.
- Multi-tenant / external organization accounts.
- Mobile native app.
- Advanced reporting, data export, and regulatory submission.
- Horizontal scaling, high availability, and disaster recovery beyond basic rollback.

---

## 8. Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Scope creep beyond 15 weeks | Missed deadline | Frozen MVP scope (this section); deferred list is explicit |
| Apple Silicon VM issues | Blocked provisioning demos | Use Docker Compose as primary target; Vagrant/QEMU only if a real VM is needed |
| Toolchain version conflicts (Java/Tomcat/Maven) | Build failures | Pin versions in `pom.xml` and Docker images; verify locally before Jenkins |
| Manual git steps mis-executed | Broken repo history | I prepare exact commands; you run them; we verify before and after |
| Test flakiness (Selenium) | False pipeline failures | Stable selectors, explicit waits, failure screenshots |
