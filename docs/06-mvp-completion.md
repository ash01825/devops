# Week 6 — MVP Completion

**Sprint theme:** finish all remaining Must stories: US-2.3, US-2.4, US-3.1, US-3.2,
US-3.3, US-4.1 (+ US-1.2 user/role management via seeded roles).

## What was added on top of Week 5

- **Edit measurements** (US-2.3) — `SampleFormController.editForm` (`GET /samples/{id}/edit`)
  + `update` (`POST /samples/{id}`) via `SampleService.update`; APPROVED samples are
  immutable (`IllegalStateException`).
- **Search** (US-2.4) — `SampleViewController.list` (`GET /samples?q&status&stationId&from&to`)
  delegates to `SampleService.search`; empty results render the `noResults` message
  (asserted by Selenium journey 3).
- **Role workflow** (US-3.1/US-3.2) — `SampleWorkflowController` (`POST /samples/{id}/status`)
  calls `SampleService.transition`, which enforces `allowedRoles` and requires a comment
  for REJECTED. Path: `DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED`,
  plus `REJECTED → DRAFT` rework.
- **Audit history** (US-3.3) — every create/transition writes a `StatusHistory` row
  (from/to, changedBy, changedAt, comment); shown in `samples/view` (`#historyTable`).
- **Dashboard** (US-4.1) — `DashboardController` (`GET /dashboard`) renders
  `countsByStatus`, `countsByStation`, and `recent(8)`; JSON at `/api/dashboard/summary`.
- **Stations admin CRUD** (FR-6) — `StationController` (`/stations/**`, `ADMIN` only
  per `SecurityConfig`) backed by `StationService`.
- **JSON API** — `ApiController`: `GET /api/samples?search&status&station`,
  `GET /api/samples/{id}`, `GET /api/dashboard/summary`.

## Role–permission matrix

| Transition | COLLECTOR | ANALYST | REVIEWER | ADMIN |
|------------|:---------:|:-------:|:--------:|:-----:|
| `DRAFT → SUBMITTED` | ✅ | ❌ | ❌ | ✅ |
| `SUBMITTED → UNDER_REVIEW` | ❌ | ✅ | ✅ | ✅ |
| `UNDER_REVIEW → APPROVED` | ❌ | ❌ | ✅ | ✅ |
| `UNDER_REVIEW → REJECTED` (comment required) | ❌ | ❌ | ✅ | ✅ |
| `REJECTED → DRAFT` (rework) | ✅ | ❌ | ❌ | ✅ |
| Edit measurements (non-APPROVED) | ✅ | ✅ | ✅ | ✅ |
| Stations CRUD (`/stations/**`) | ❌ | ❌ | ❌ | ✅ |

Illegal transitions throw `IllegalStateException`; wrong role throws `SecurityException`.

## Tag v1.0.0

```bash
git tag -a v1.0.0 -m "MVP complete: all Must stories"
git push origin v1.0.0
```

`APP_VERSION` in `jenkins/Jenkinsfile` resolves from `git describe --tags --always`.

## Backlog update

All 13 Must stories (45 pts, see `docs/02-agile-devops-workflow.md`) are Done:
US-1.1, US-1.2, US-2.1, US-2.2, US-2.3, US-2.4, US-3.1, US-3.2, US-3.3, US-4.1,
US-5.1, US-5.2, US-5.3. Deferred items stay in `docs/01-problem-statement.md` §7.
