#!/usr/bin/env bash
# ============================================================
# Water Quality Sampling Portal — end-to-end demo script
# Runs: mvn verify -> docker build -> compose up -> health check
# Selenium note printed at the end (needs Chrome + driver).
# Usage: bash scripts/demo.sh
# ============================================================
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

APP_VERSION="${APP_VERSION:-1.0.0}"
BASE_URL="http://localhost:8080/water-quality-portal"

echo "=== [1/5] Maven full verify (unit + Selenium journeys) ==="
cd water-quality-portal
if [ "${SKIP_SELENIUM:-false}" = "true" ]; then
  echo "(SKIP_SELENIUM=true: Selenium journeys excluded)"
  mvn -B test -Dtest='!WaterQualitySeleniumTest'
else
  mvn -B verify
fi
cd ..

echo "=== [2/5] Docker build (versioned tag) ==="
docker build -f docker/Dockerfile -t "water-quality-portal:${APP_VERSION}" .
docker tag "water-quality-portal:${APP_VERSION}" water-quality-portal:latest

echo "=== [3/5] Compose up (same version just built) ==="
APP_VERSION="${APP_VERSION}" docker compose -f docker/docker-compose.yml up -d --build
docker compose -f docker/docker-compose.yml ps

echo "=== [4/5] Health check (waits up to ~2 min for Tomcat) ==="
for i in $(seq 1 24); do
  if curl -sf "$BASE_URL/actuator/health" >/dev/null; then
    echo "App is up."
    break
  fi
  if [ "$i" = "24" ]; then
    echo "App did not become healthy in time." >&2
    exit 1
  fi
  sleep 5
done
if command -v conda >/dev/null 2>&1; then
  conda run -n os python scripts/health_check.py --base-url "$BASE_URL"
else
  python3 scripts/health_check.py --base-url "$BASE_URL"
fi

if [ "${SKIP_SELENIUM:-false}" = "true" ]; then
  echo "=== [5/5] Selenium suite SKIPPED (SKIP_SELENIUM=true) ==="
else
  echo "=== [5/5] Selenium suite (already ran inside mvn verify) ==="
fi
cat <<'EOF'
Selenium journeys (headless Chrome via WebDriverManager, no manual driver setup):
  cd water-quality-portal
  mvn -B test -Dtest='WaterQualitySeleniumTest'   # 5 journeys: login, create,
                                                  # search, workflow, dashboard
Failure artefacts: target/selenium-failures/ (screenshot + page-source HTML).
Jenkins runs the same class in the 'Selenium Quality Gate' stage; pass
SKIP_SELENIUM=true only if the agent has no browser at all.
EOF

echo "=== Demo complete: $BASE_URL (image water-quality-portal:${APP_VERSION}) ==="
