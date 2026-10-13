# Viva presentation — talk track (10 slides, ~10 minutes)

Use `docs/DEMO.md` for the live clicks; this file is the narration.

1. **Problem** — paper + spreadsheets + email; no single source of truth.
   (`docs/01-problem-statement.md`)
2. **Users & scope** — COLLECTOR/ANALYST/REVIEWER/ADMIN; frozen 15-week MVP.
3. **Workflow** — `DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED`
   with role guards + audit history. Draw it on the board.
4. **Live demo 1** — login as collector → create `WQ-0101` → dashboard counts move.
5. **Live demo 2** — submit → analyst starts review → reviewer approves;
   show history table (who/when/why).
6. **Pipeline** — `git push → Jenkins → Selenium gate → Docker → Ansible`
   (`jenkins/Jenkinsfile` stages on screen).
7. **Quality gate** — show `docs/evidence/gate-fail/`: red run stops before
   deploy; fix commit; green rerun. "Bad code cannot reach the server."
8. **Containers & provisioning** — versioned image, `changed=0` rerun,
   health `UP`, rollback. (`docs/evidence/`)
9. **Evidence map** — `docs/evidence/` listing: rehearsal log, lifecycle log,
   ansible logs, screenshots, kanban snapshot.
10. **Limits & next** — single node, no registry push in lab, Jenkins UI on lab
    machine; future: GIS maps, sensor ingest, exports. (`docs/15-final-release.md`)

Anticipated questions are answered in `docs/15-final-release.md` (viva Q&A).
