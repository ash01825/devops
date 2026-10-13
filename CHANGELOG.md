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
- Quality gates: 31 tests (JUnit + headless-Chrome Selenium); failures abort
  before packaging/deploy stages.
- Provisioning: Ansible playbook (idempotent), rollback playbook, health checks.

## Unreleased (on `master` after `v1.0.0`)

- Docs and pipeline hardening; see `git log --oneline` for details.
