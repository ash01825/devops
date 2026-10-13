# Quality-gate failure evidence (Week 10)

Deliberately introduced defect (then reverted): `SampleService.allowedRoles`
returned `null` for `DRAFT → SUBMITTED`, so no collector could submit anything.

## Red run (defect present)

- `unit-red.log` — `SampleServiceTest#fullWorkflowSucceedsWithRightRoles`:
  `IllegalStateException: Illegal transition: DRAFT -> SUBMITTED`, BUILD FAILURE.
- `selenium-red.log` + `selenium-failure.png` / `.html` —
  `journey4_collectorSubmitsDraft`: the Submit button never appears, Selenium
  times out, screenshot + page source auto-saved. A failed gate aborts the
  Jenkins pipeline before Archive/Docker/Deploy, so this build could never ship.

## Fix + green rerun

- Fix: restore `Set.of(Role.COLLECTOR, Role.ADMIN)` for `DRAFT → SUBMITTED`
  (one-line revert; the hardening around it is committed on this branch).
- Green proof: `docs/evidence/jenkins-pipeline-rehearsal.log` (31 tests pass)
  and the rerun below was committed as part of the normal suite.
