# Changelog — Water Quality Sampling Portal

## v1.0.0 — MVP release (tagged `v1.0.0`)

- Samples: create, view, update (`DRAFT`/`REJECTED` only), search
  (keyword, status, station, collection date).
- Role workflow `DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED`
  (+ `REJECTED → DRAFT` rework) with full audit history; UI shows only
  role-permitted actions with human-readable labels.
- Dashboard: counts by status/station, recent samples (page + JSON API).
- Station management (`ADMIN` only, URL + method-level guards).
- Pipeline: Jenkinsfile (build → unit → Selenium gate → archive →
  versioned Docker image → compose deploy with health gate).
- Quality gates: 32 tests (JUnit + headless-Chrome Selenium); failures abort
  before packaging/deploy stages.
- Provisioning: Ansible playbook (idempotent), rollback playbook, health checks.

## v1.0.1 — viva hardening (tagged `v1.0.1`)

- Security: new samples always start `DRAFT` (posted status ignored);
  server-side measurement/coordinate/date validation; edit form guarded
  on GET as well as POST; prod login hides demo accounts.
- Reliability: prod bootstrap admin from `ADMIN_PASSWORD`; Ansible handler
  flush before health-gate; `ADMIN_PASSWORD`/DB vars threaded through
  Jenkins → compose → Ansible; Dockerfile uses `$CATALINA_HOME`.
- Tests: 32 green (Selenium search-submit hardened for headless Chrome).

## Unreleased

- See `git log --oneline` for details.
