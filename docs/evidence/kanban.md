# Kanban snapshot — end of Sprint 15 (all MVP stories Done)

Columns: `Backlog → Ready → In Progress → In Review → Done`.
Snapshotted from `docs/02-agile-devops-workflow.md` §2–§3 after the v1.0.0 release.

## Done ✅

| ID | Story | Evidence |
|----|-------|----------|
| US-1.1 | Login/logout, 4 roles | `LoginController`, `login.html`, Selenium j1 |
| US-1.2 | User/role management | Seeded roles + `ADMIN` station guard |
| US-2.1 | Create sample | `SampleFormController.create`, Selenium j2 |
| US-2.2 | View sample detail | `SampleViewController.view`, history table |
| US-2.3 | Update measurements | Edit form, `DRAFT`/`REJECTED` guard + tests |
| US-2.4 | Search samples | Filter form, `noResults` state, Selenium j3 |
| US-3.1 | Submit draft | `DRAFT → SUBMITTED`, Selenium j4 |
| US-3.2 | Approve/reject | Reviewer actions, comment-required reject |
| US-3.3 | Status history | `StatusHistory` on every create/transition |
| US-4.1 | Summary dashboard | `DashboardController`, Selenium j5 |
| US-5.1 | Jenkins CI | `Jenkinsfile`, rehearsal log |
| US-5.2 | Test quality gate | `docs/evidence/gate-fail/` red→green |
| US-5.3 | Docker + Ansible CD | Image `1.0.0`, `changed=0`, rollback healthy |

## Backlog / Ready / In Progress / In Review

Empty — MVP scope frozen (`docs/01-problem-statement.md` §7). Deferred items
(IoT ingest, GIS maps, public portal, mobile app, advanced reporting) stay out
of scope; see `docs/15-final-release.md` for the future plan.
