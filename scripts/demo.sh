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

echo "=== [1/5] Maven full verify (unit + integration tests) ==="
cd water-quality-portal
mvn -B verify
cd ..

echo "=== [2/5] Docker build (versioned tag) ==="
docker build -f docker/Dockerfile -t "water-quality-portal:${APP_VERSION}" .
docker tag "water-quality-portal:${APP_VERSION}" water-quality-portal:latest

echo "=== [3/5] Compose up ==="
docker compose -f docker/docker-compose.yml up -d --build
docker compose -f docker/docker-compose.yml ps

echo "=== [4/5] Health check ==="
if command -v conda >/dev/null 2>&1; then
  conda run -n os python scripts/health_check.py --base-url "$BASE_URL"
else
  python3 scripts/health_check.py --base-url "$BASE_URL"
fi

echo "=== [5/5] Selenium note ==="
cat <<'EOF'
Selenium E2E (requires Chrome/Chromium + matching chromedriver on PATH):
  cd water-quality-portal
  mvn -B verify -Dtest=SeleniumSuite -DfailIfNoTests=false || mvn -B verify
Headless CI: export HEADLESS=true (see suite config). Jenkins runs this in the
'Selenium Quality Gate' stage; pass SKIP_SELENIUM=true only if no browser exists.
EOF

echo "=== Demo complete: $BASE_URL (image water-quality-portal:${APP_VERSION}) ==="
