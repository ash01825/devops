# Jenkins Job Configuration — Water Quality Sampling Portal

Step-by-step setup for the `jenkins/Jenkinsfile` pipeline.
Companion doc: `jenkins/README.md` (install + plugin + trigger reference).

## 1. Prerequisites

- Jenkins 2.440+ with JDK 21 available to agents (Temurin 21 recommended).
- Git, Maven 3.9+, Docker 24+ on the agent that runs this job.
- This repo checked into Git (local path or GitHub remote).

## 2. Required plugins

Install via **Manage Jenkins → Plugins → Available plugins**:

| Plugin | ID | Why |
|---|---|---|
| Git | `git` | `checkout scm` |
| Pipeline | `workflow-aggregator` | Declarative pipeline |
| JUnit | `junit` | `junit` test publishing |
| HTML Publisher | `htmlpublisher` | Selenium/Serenity HTML reports (optional) |
| Docker Pipeline | `docker-workflow` | `docker.*` steps / `withRegistry` (optional for push) |

```text
Manage Jenkins → Plugins → Available plugins → search each ID → Install
```

Optional HTML report stage snippet (only if the plugin is installed):

```groovy
publishHTML(target: [
  reportDir: 'water-quality-portal/target/site/serenity',
  reportFiles: 'index.html',
  reportName: 'Selenium Report',
  allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true
])
```

## 3. Create the pipeline job

1. **New Item** → name `water-quality-portal` → type **Pipeline** → OK.
2. **General** → Description: `Water Quality Sampling Portal CI/CD`.
3. **Build Triggers** → check **Poll SCM** → Schedule: `H/2 * * * *`
   (polls the repo every ~2 min; evidence: build `Changes` list + polling log).
4. **Pipeline** section:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git** → Repository URL: `<your-repo-url>` (or local path for PoC)
   - Branch: `*/main` (or your default branch)
   - Script Path: `jenkins/Jenkinsfile`
5. **Parameters**: the Jenkinsfile declares `ENV_NAME` (default `staging`)
   and `SKIP_SELENIUM` (default `false`) — no manual parameter config needed.
6. **Save** → **Build Now** (first run registers the parameters).

## 4. Archive artefact config

Handled in-pipeline (no UI config needed):

- `Archive` stage: `archiveArtifacts 'water-quality-portal/target/*.war'`
- `post.always`: re-archives WAR + publishes JUnit XML.
- Verify per build: **Build page → Build Artifacts** shows
  `water-quality-portal/target/water-quality-portal.war`.

## 5. Trigger evidence (what to screenshot for the report)

- [ ] Job config page showing **Pipeline script from SCM** + Script Path
      `jenkins/Jenkinsfile`. → placeholder: `docs/screenshots/jenkins-job-config.png`
- [ ] **Poll SCM** schedule `H/2 * * * *`. → placeholder: `docs/screenshots/jenkins-trigger.png`
- [ ] Build **Console Output** showing stages incl. `Selenium Quality Gate`.
      → placeholder: `docs/screenshots/jenkins-console.png`
- [ ] Build **Test Result** page (JUnit trend, failures if any).
      → placeholder: `docs/screenshots/jenkins-tests.png`
- [ ] **Build Artifacts** listing the `.war`. → placeholder: `docs/screenshots/jenkins-artifacts.png`
- [ ] Failed-test run proving **Deploy is skipped** (red build, no Deploy stage
      in console). → placeholder: `docs/screenshots/jenkins-gate-fail.png`

## 6. Quality-gate behaviour

Declarative pipelines abort on first stage failure, so:

- `Unit Test` failure → build red, `Archive/Docker/Deploy` never run.
- `Selenium Quality Gate` failure → same; set `SKIP_SELENIUM=true` only for
  agents without a browser/driver, never to hide a red suite.
