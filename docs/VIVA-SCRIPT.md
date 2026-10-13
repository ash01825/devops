# VIVA / LAB DEMO SCRIPT — read this, click that (10–12 minutes)

You don't need to understand everything. Follow the bold steps, say the quoted
lines, and open the files named. Everything below was verified working.

## 0. Before she arrives (5 min setup)

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
conda activate os
cd /Users/ash/Desktop/devops/water-quality-portal
mvn spring-boot:run        # wait for "Started WaterQualityPortalApplication"
```

Open `http://localhost:8080/water-quality-portal` in Chrome. Keep this file open
on your phone. Keep `docs/screenshots/` open in a tab as backup (if the live app
freezes, show the screenshots — same screens).

## 1. Opening (30 seconds)

Say: "I built a **Water Quality Sampling Portal** — field collectors record
samples, analysts enter lab results, reviewers approve or reject, and everything
moves through a **role-based workflow**: Draft, Submitted, Under Review, and then
Approved or Rejected, with a full audit history. It is delivered through a
**DevOps pipeline**: Git to Jenkins to Selenium tests to Docker to Ansible."

## 2. Live app demo (5 minutes)

**Login page.** Say: "Four roles, one login." Type `collector` / `collector123`.
Say: "As a collector I land on the **dashboard** — counts by status, by station,
and recent samples."

**Samples → New Sample.** Fill: Sample ID `WQ-0101`, Station `ST-01`,
Collected At yesterday, Collector your name, pH `7.2`, notes `Lab demo`.
Click **Create sample**. Say: "New records always start as **Draft**, and the
history table already has its first entry."

**Workflow.** Click **Submit for Review**. Say: "Only the actions my role is
allowed are shown — a collector only sees Submit." Log out. Log in as
`analyst` / `analyst123`, open WQ-0101, click **Start Review**. Log out. Log in
as `reviewer` / `reviewer123`, open WQ-0101, click **Approve**. Point at the
history table. Say: "Every transition records who, when, and why. Rejecting
requires a comment — the server enforces it, not just the page."

**Admin (optional, 30 sec).** Log in as `admin` / `admin123`, open **Stations**.
Say: "Station master data is admin-only — collectors get a 403 here."

If she clicks something weird (bad ID, wrong role): "That renders our friendly
error page / is blocked server-side — try it." (Both are handled, don't fear it.)

## 3. Pipeline (3 minutes, show files)

Open `jenkins/Jenkinsfile`. Say: "Every push runs this: **build, unit tests,
Selenium gate, archive, Docker build, deploy** — and a red stage stops
everything below it, so bad code can never deploy."

Open `docs/evidence/jenkins-pipeline-rehearsal.log`. Say: "32 tests green —
unit plus five real browser journeys: login, create, search, workflow,
dashboard. Failures save a screenshot automatically."

Open `docs/evidence/gate-fail/`. Say: "I deliberately broke the workflow to
prove the gate: red build, screenshot captured, one-line fix, green rerun."

Open a terminal: `docker images | grep water-quality-portal`. Say: "Versioned
image, 904 MB, deployed to Tomcat with Postgres. Health endpoint says UP."
Show `docs/screenshots/` if she wants static proof.

Say: "Provisioning is Ansible — reruns report `changed=0`, health-gated, with a
rollback playbook. Evidence: `docs/evidence/ansible-*.log`."

## 4. Git (1 minute)

Run `git log --oneline -8`. Say: "Small meaningful commits, a feature branch
with a real pull request — number 1 on GitHub, with review discussion — merged,
and release tag `v1.0.0`." (Show `git tag -l` and the PR page if asked.)

## 5. Close BEFORE she critiques (30 seconds)

Say first: "Known limits, honestly: single-node deployment, no external Docker
registry push in the lab, Jenkins screenshots get captured on the lab machine —
and next I'd add GIS maps and sensor ingest." Naming limits yourself removes
her ammunition.

## 6. Likely questions (one-line answers)

- **Why WAR + Tomcat, not JAR?** "The rubric asks for a Tomcat deployment target; WAR packaging deploys straight onto Tomcat 10.1."
- **How do failed tests block deployment?** "Declarative pipeline aborts on first red stage — Deploy is unreachable. Proven in gate-fail evidence."
- **What if two people edit the same sample?** "Only Draft/Rejected samples are editable; approved ones are immutable; every change is audited."
- **Passwords?** "BCrypt-hashed, never plaintext. Prod creates its first admin only from an env variable — no default password."
- **Idempotency?** "Ansible rerun reports changed=0 — the log is committed."
- **Rollback?** "Rollback playbook switches the compose stack to the previous image tag and health-gates it."
- **Why did the suite fail once?** "Headless-Chrome click flakiness after a browser update — diagnosed with screenshots, fixed via requestSubmit, green since."
- **Registry?** "Versioned image in the local daemon, ready to push — push snippet is in the Jenkinsfile for when a registry exists."

## 7. If the live demo breaks

Don't panic. Say: "Let me show the captured run," and open `docs/screenshots/`
(dashboard → samples → detail) plus `docs/evidence/jenkins-pipeline-rehearsal.log`.
The evidence carries the demo.
