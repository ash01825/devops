# Lab Demonstration Script

Environment notes (run first):

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
conda activate os   # helper toolchain (ansible, health check)
java -version       # must show 21 (Temurin)
```

Demo logins: `admin/admin123`, `collector/collector123`, `analyst/analyst123`,
`reviewer/reviewer123`. App: `http://localhost:8080/water-quality-portal`.

## 1. Build + tests all green

```bash
cd water-quality-portal
mvn -B verify
```

Expected: `BUILD SUCCESS`, surefire + failsafe reports, Selenium screenshots dir
`target/selenium-failures/` empty (only created on failure).

## 2. Login → create → submit → approve click-path

1. `mvn spring-boot:run` → open `/login`.
2. Login `collector/collector123` → `/dashboard`.
3. **Samples → New Sample**: ID `WQ-0101`, station `ST-01`, collectedAt yesterday,
   collector name, pH `7.2` → Save. Expected: detail page, `#statusBadge` = `DRAFT`,
   flash `Sample WQ-0101 created.`
4. Click **Submit** (`btn-SUBMITTED`). Expected: badge `SUBMITTED` + history row.
5. Logout; login `analyst/analyst123` → open WQ-0101 → **Start Review**.
   Expected: `UNDER_REVIEW`.
6. Logout; login `reviewer/reviewer123` → open WQ-0101 → **Approve**.
   Expected: `APPROVED`, history shows all transitions with who/when.

## 3. Dashboard counts

Open `/dashboard` (or `GET /api/dashboard/summary`). Expected: `#summaryHeading`
with totals, `#statusCards .stat` per status, `#stationTable` and `#recentTable`
non-empty; counts shift after step 2 (one more `DRAFT`, then moved statuses).

## 4. Docker build + compose up + health check

```bash
cd /Users/ash/Desktop/devops
docker build -f docker/Dockerfile -t water-quality-portal:1.0.0 .
docker images | grep water-quality-portal   # expect 1.0.0 + latest
docker compose -f docker/docker-compose.yml up -d --build
conda run -n os python scripts/health_check.py   # expect HEALTHY
curl -f http://localhost:8080/water-quality-portal/actuator/health
```

## 5. Ansible syntax-check + playbook summary

```bash
conda run -n os ansible-playbook -i ansible/inventory.ini ansible/playbook.yml --syntax-check
conda run -n os ansible-playbook -i ansible/inventory.ini ansible/playbook.yml
conda run -n os ansible-playbook -i ansible/inventory.ini ansible/playbook.yml   # 2nd: changed=0
```

Expected: first run `failed=0`, second run `changed=0`. Playbook ships compose to
`/opt/wq-portal`, starts `db+app`, gates on actuator 200. Rollback:
`ansible/rollback.yml -e prev_version=1.0.0`.

## 6. Jenkinsfile walkthrough

Open `jenkins/Jenkinsfile`: point at Checkout → Build → Unit Test →
Selenium Quality Gate → Archive → Docker Build/Publish → Deploy;
`ENV_NAME`/`SKIP_SELENIUM` params; `APP_VERSION` from `git describe`;
`post` success/failure messages. Job config: `jenkins/job-config.md`.

## 7. Tag / log evidence

```bash
git log --oneline -8
git tag -l                      # expect v1.0.0
git show v1.0.0 --stat | head
```

Expected: `feat/fix/docs/ci` commits on `master`, tag `v1.0.0` on MVP completion.
