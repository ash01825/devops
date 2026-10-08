# Week 7 — Jenkins CI

**Goal (US-5.1):** every commit to `master` triggers an automated Maven build + test,
with the WAR archived. Full reference: `jenkins/README.md`, `jenkins/job-config.md`.

## 1. Install Jenkins

**Option A — Homebrew (macOS):**

```bash
brew install jenkins-lts
brew services start jenkins-lts
open http://localhost:8081
cat ~/.jenkins/secrets/initialAdminPassword
```

**Option B — Docker:**

```bash
docker run -d --name jenkins -p 8081:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk21
docker logs -f jenkins   # copy the initial admin password
```

Jenkins uses host port **8081** (the app uses 8080). Agent needs JDK 21, Maven 3.9+,
Docker, Git: `brew install --cask temurin21 && brew install maven git docker`.

## 2. Required plugins

Manage Jenkins → Plugins → Available: **Git** (`git`), **Pipeline**
(`workflow-aggregator`), **JUnit** (`junit`). Optional: HTML Publisher
(`htmlpublisher`), Docker Pipeline (`docker-workflow`).

## 3. Job setup (Pipeline from SCM)

1. New Item → `water-quality-portal` → **Pipeline** → OK.
2. Pipeline → Definition: **Pipeline script from SCM** → SCM **Git** → Repository URL.
3. Script Path: `jenkins/Jenkinsfile`. Branch: your default (e.g. `*/master`).
4. Build Triggers → **Poll SCM** → Schedule: `H/2 * * * *` (picks up commits ~2 min).
5. Save → **Build Now** (first run registers `ENV_NAME` / `SKIP_SELENIUM` params).

## 4. What the job runs

`checkout scm` → `mvn -B clean package -DskipTests` → `mvn -B test` (JUnit publish)
→ Selenium gate → Archive → Docker stages → Deploy (Weeks 8+). Poll trigger evidence:
build **Changes** list + job **Git Polling Log**.

## 5. Artefact archive

In-pipeline: `archiveArtifacts 'water-quality-portal/target/*.war'`
(`post.always` re-archives + publishes surefire XML). Verify per build:
**Build page → Build Artifacts** shows `water-quality-portal.war`.

## 6. What SUCCESS looks like

- Console: all stages green incl. `Selenium Quality Gate`; `post.success`
  prints `Pipeline OK — version <tag> deployed to staging`.
- **Test Result** page shows JUnit trend, 0 failures.
- **Build Artifacts** lists the `.war`.
- Next commit auto-builds within ~2 min via Poll SCM.
