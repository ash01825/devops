# Jenkins — Water Quality Sampling Portal

CI/CD pipeline: `Jenkinsfile` (this folder). Detailed job setup: `job-config.md`.

## 1. Install Jenkins

### Option A — macOS (Homebrew)

```bash
brew install jenkins-lts
brew services start jenkins-lts
# UI: http://localhost:8081  (Jenkins on 8081 to avoid clashing with the app on 8080)
open http://localhost:8081
# First-run admin password:
cat ~/.jenkins/secrets/initialAdminPassword
```

Requirements on the agent: JDK 21, Maven 3.9+, Docker, Git:

```bash
brew install --cask temurin21
brew install maven git docker
docker --version && mvn -version && java -version
```

### Option B — Docker

```bash
docker run -d --name jenkins -p 8081:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk21
docker logs -f jenkins   # copy the initial admin password
open http://localhost:8081
```

> The app itself uses host port 8080, so Jenkins is mapped to **8081** here.

## 2. Plugins

Manage Jenkins → Plugins → Available: `git`, `workflow-aggregator`
(Pipeline), `junit`, `htmlpublisher` (HTML Publisher), `docker-workflow`
(Docker Pipeline). Restart Jenkins after install if prompted.

## 3. Create the job (summary)

1. New Item → `water-quality-portal` → Pipeline → OK.
2. Pipeline → Definition: **Pipeline script from SCM** → Git → repo URL.
3. Script Path: `jenkins/Jenkinsfile`. Branch: your default (`*/main`).
4. Build Triggers → **Poll SCM** → `H/2 * * * *`. Save → Build Now.

Full click-path + evidence checklist: see `job-config.md`.

## 4. Parameters & triggers

| Item | Value |
|---|---|
| `ENV_NAME` (string) | `staging` default; deploy container is named `wq-portal-app-<ENV_NAME>` |
| `SKIP_SELENIUM` (bool) | `false` default; skips Selenium gate when `true` |
| `APP_VERSION` (env) | `git describe --tags --always`, fallback `1.0.0` |
| Poll trigger | `H/2 * * * *` — any commit is picked up within ~2 min |
| Artefacts | `water-quality-portal/target/*.war` + JUnit XML in `post.always` |

Trigger evidence: each auto build shows the commit under **Changes**, and the
**Polling Log** (job page → Git Polling Log) records the poll that found it.

## 5. Local pipeline rehearsal (no Jenkins needed)

```bash
# Same commands the Jenkinsfile runs, in order:
cd water-quality-portal && mvn -B clean package -DskipTests
mvn -B test
mvn -B verify -Dtest=SeleniumSuite -DfailIfNoTests=false || mvn -B verify
cd .. && docker build -f docker/Dockerfile -t water-quality-portal:1.0.0 .
docker compose -f docker/docker-compose.yml up -d
curl -f http://localhost:8080/water-quality-portal/actuator/health
```
