# Water Quality Sampling Portal — Agile Planning & DevOps Workflow

**Week 2 Deliverable**
**Project:** DevOps Pipeline for a Water Quality Sampling Portal

---

## 1. User Stories & Acceptance Criteria

Stories are written in the standard format: *As a [role], I want [capability] so that
[benefit].* Each story has acceptance criteria (AC) that must pass before the story is
"done".

### Epic 1 — Authentication & Roles

**US-1.1** As a user, I want to log in with my username and password so that I can access
role-appropriate features.
- AC: Valid credentials open the dashboard; invalid credentials show an error.
- AC: Passwords are hashed, never stored in plaintext.
- AC: Logout returns to the login page.

**US-1.2** As an admin, I want to manage users and assign roles so that only authorized
people perform specific actions.
- AC: Admin can create/edit/deactivate users.
- AC: Each user has exactly one of `COLLECTOR`, `ANALYST`, `REVIEWER`, `ADMIN`.

### Epic 2 — Sample Records

**US-2.1** As a collector, I want to create a sample record so that I capture collection
details in one place.
- AC: Form captures station, collection timestamp, collector, and notes.
- AC: New sample starts in `DRAFT` status.
- AC: Invalid or missing required fields are rejected with clear messages.

**US-2.2** As a user, I want to view a sample's full details so that I can see its
measurements and history.
- AC: Detail page shows all fields plus the status history log.

**US-2.3** As an analyst, I want to update sample measurements so that results are recorded
accurately.
- AC: Analyst can edit ph, temperature, dissolved oxygen, turbidity, and conductivity.
- AC: Edits are saved and reflected immediately in the detail view.

**US-2.4** As a user, I want to search samples by sample ID, status, station, or date so
that I can quickly find records.
- AC: Results filter correctly by each criterion and combinations.
- AC: Empty result set shows a friendly "no results" message.

### Epic 3 — Role-Based Status Workflow

**US-3.1** As a collector, I want to submit a draft sample so that it moves into the review
queue.
- AC: `DRAFT → SUBMITTED` transition available only to collector (or admin).
- AC: Submission records a history entry.

**US-3.2** As a reviewer, I want to approve or reject a submitted sample so that the
workflow reaches a terminal state.
- AC: `SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED` transitions available to reviewer.
- AC: Rejection requires a comment; approval records who/when.
- AC: Unauthorized roles cannot perform transitions.

**US-3.3** As a user, I want to see the full status history so that I can audit every
change.
- AC: History shows from/to status, changed_by, timestamp, and comment.

### Epic 4 — Summary Dashboard

**US-4.1** As a user, I want a summary dashboard so that I can see sample counts at a
glance.
- AC: Dashboard shows counts by status, station, and collection date.
- AC: Counts update after a sample is created or transitions status.

### Epic 5 — DevOps Pipeline

**US-5.1** As a developer, I want every commit to `develop` to trigger an automated build
and test so that defects are caught early.
- AC: Jenkins polls the repo and runs `mvn clean verify`.
- AC: Build artifact is archived.

**US-5.2** As a developer, I want failed tests to block deployment so that bad code never
reaches the target.
- AC: Selenium failures fail the pipeline before the deploy stage.

**US-5.3** As an operator, I want a versioned Docker image and idempotent provisioning so
that releases are reproducible and rollback is possible.
- AC: Each release produces a tagged image.
- AC: Ansible rerun reports no changes (`changed=0`).
- AC: Rollback to previous image restores a healthy endpoint.

---

## 2. Product Backlog (Prioritized)

Priority = MoSCoW. Story points are rough relative sizing.

| ID | Story | Priority | Points | Depends on |
|----|-------|----------|--------|------------|
| US-1.1 | Login/logout | Must | 3 | — |
| US-1.2 | User/role management | Must | 3 | US-1.1 |
| US-2.1 | Create sample | Must | 5 | US-1.1 |
| US-2.2 | View sample detail | Must | 3 | US-2.1 |
| US-2.3 | Update measurements | Must | 3 | US-2.2 |
| US-2.4 | Search samples | Must | 5 | US-2.1 |
| US-3.1 | Submit draft | Must | 2 | US-2.1 |
| US-3.2 | Approve/reject | Must | 3 | US-3.1 |
| US-3.3 | Status history | Must | 2 | US-3.2 |
| US-4.1 | Summary dashboard | Must | 3 | US-2.1, US-3.2 |
| US-5.1 | Jenkins CI | Must | 5 | US-1..4 |
| US-5.2 | Test quality gate | Must | 3 | US-5.1 |
| US-5.3 | Docker + Ansible CD | Must | 5 | US-5.2 |

**Total MVP points:** 45 (13 stories).

---

## 3. 15-Week Kanban / Scrum Plan

Model: **Scrum** with one-week sprints (fits the 15-week fixed timeline). A **Kanban board**
tracks the backlog → in-progress → done flow; the sprint plan below maps stories to weeks.

| Week | Sprint theme | Stories / deliverables |
|------|--------------|------------------------|
| 1 | Problem & scope | — (foundation) |
| 2 | Agile/DevOps workflow | Backlog, board, DoD, lifecycle diagram |
| 3 | Architecture & setup | SRS, architecture, data model, local skeleton |
| 4 | Git/GitHub init | Repo, README, templates, branch rules |
| 5 | Feature 1 (branch) | US-1.1, US-2.1, US-2.2 (create/view + login) |
| 6 | MVP completion | US-2.3, US-2.4, US-3.1, US-3.2, US-3.3, US-4.1 |
| 7 | CI job | US-5.1 (Jenkins Maven job) |
| 8 | Pipeline-as-code | US-5.1 (Jenkinsfile + deploy) |
| 9 | Selenium design | Test plan + local Selenium |
| 10 | Continuous testing | US-5.2 (quality gate) |
| 11 | Docker lifecycle | Dockerfile + container ops |
| 12 | Jenkins–Docker CD | US-5.3 (versioned image deploy) |
| 13 | Config management | Ansible playbook |
| 14 | Provisioning + reliability | Idempotency, health check, rollback |
| 15 | Release + docs + viva | Final report + live demo |

### Kanban board columns

`Backlog → Ready → In Progress → In Review → Done`

---

## 4. Definition of Done (DoD)

A user story is **Done** only when ALL of the following are true:

1. **Code complete** against the story's acceptance criteria.
2. **Builds clean** with `mvn clean verify` (no compile/unit-test failures).
3. **Tests pass** — relevant unit tests and (where applicable) Selenium journey tests.
4. **Reviewed** via pull request (feature branch → `develop`) with at least one review.
5. **Merged** into `develop` by the repository owner (user runs git).
6. **Documented** — any API/DB change reflected in `docs/`.
7. **Deployable** — no broken pipeline stage; artifact/container produced.
8. **Evidence captured** — screenshots/logs saved for the deliverable.

For **DevOps deliverables** (Jenkins, Docker, Ansible), the DoD additionally requires:

9. **Repeatable** — the job/playbook can be re-run with the same result (idempotent).
10. **Demonstrated** — a run log/screenshot proves success, not just configuration.

---

## 5. DevOps Lifecycle Diagram (development → operations)

```
┌─────────────────────────────── PLAN ───────────────────────────────┐
│  Problem → stories → backlog → sprint plan → DoD (Weeks 1–2)        │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
┌─────────────────────────────── CODE ───────────────────────────────┐
│  Spring Boot + PostgreSQL, feature branches, PR review (Weeks 3–6)  │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ git commit/push (you run)
                                ▼
┌─────────────────────────────── BUILD ──────────────────────────────┐
│  Jenkins: checkout → mvn clean package → archive WAR (Weeks 7–8)    │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
┌─────────────────────────────── TEST ───────────────────────────────┐
│  Unit tests + Selenium quality gate; failure blocks deploy (Wk 9-10)│
└───────────────────────────────┬─────────────────────────────────────┘
                                │
┌─────────────────────────────── RELEASE ────────────────────────────┐
│  Versioned Docker image, published to registry (Weeks 11–12)        │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
┌─────────────────────────────── DEPLOY ─────────────────────────────┐
│  Fresh container auto-deployed to target node (Weeks 12)            │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
┌─────────────────────────────── OPERATE ────────────────────────────┐
│  Ansible provisioning, health check, idempotency, rollback (Wk 13-14)│
└───────────────────────────────┬─────────────────────────────────────┘
                                │
┌─────────────────────────────── MONITOR ────────────────────────────┐
│  Health endpoint, logs, dashboard status; continuous feedback loop   │
└─────────────────────────────────────────────────────────────────────┘
                                │ feedback → back to PLAN
```

---

## 6. Sprint Cadence & Ceremonies

| Ceremony | Cadence | Purpose |
|----------|---------|---------|
| Sprint planning | Start of each week | Select stories for the week |
| Daily standup | Daily (short) | Progress, blockers |
| Sprint review | End of each week | Demo + checkpoint for approval |
| Retrospective | End of each week | What went well / to improve |
| Backlog refinement | Weeks 2, 6, 12 | Re-prioritize as needed |
