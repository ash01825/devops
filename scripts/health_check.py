#!/usr/bin/env python3
"""Health check for the Water Quality Sampling Portal.

Checks:
  1. Spring Boot Actuator health endpoint (expects {"status":"UP"}).
  2. Security gate: /samples must NOT serve data anonymously — it must either
     redirect to /login (302, auth gate working) or serve the page (200).
  3. Login page renders (expects HTTP 200 with the sign-in form).

Redirects are NOT followed for /samples, so a bare login page can never
masquerade as a reachable app page.

Usage:
  conda run -n os python scripts/health_check.py [--base-url URL]

Exit codes: 0 = healthy, 1 = unhealthy/error.
Stdlib only (urllib) — no third-party dependencies.
"""

import argparse
import sys
import urllib.request
import urllib.error

DEFAULT_BASE = "http://localhost:8080/water-quality-portal"


class NoRedirect(urllib.request.HTTPRedirectHandler):
    """Refuse to follow redirects so 302s stay visible to the caller."""

    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


def fetch(url: str, timeout: int = 10, follow_redirects: bool = True):
    """GET url, return (status_code, body_text). Never raises."""
    try:
        if follow_redirects:
            opener = urllib.request.build_opener()
        else:
            opener = urllib.request.build_opener(NoRedirect)
        with opener.open(url, timeout=timeout) as resp:
            return resp.status, resp.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as exc:
        try:
            body = exc.read().decode("utf-8", "replace")
        except Exception:
            body = ""
        return exc.code, body
    except Exception as exc:  # connection refused, DNS, timeout, ...
        return None, f"{type(exc).__name__}: {exc}"


def main() -> int:
    parser = argparse.ArgumentParser(description="Portal health check")
    parser.add_argument("--base-url", default=DEFAULT_BASE,
                        help=f"Portal base URL (default: {DEFAULT_BASE})")
    args = parser.parse_args()
    base = args.base_url.rstrip("/")

    failures = []

    # 1. Actuator health
    status, body = fetch(f"{base}/actuator/health")
    print(f"[health] GET {base}/actuator/health -> {status}")
    if status == 200 and '"UP"' in body.replace("'", '"'):
        print("[health] OK: status UP")
    else:
        print(f"[health] FAIL: body={body[:300]!r}")
        failures.append("actuator")

    # 2. Security gate on /samples (no redirect-following here)
    status, body = fetch(f"{base}/samples", follow_redirects=False)
    print(f"[auth]   GET {base}/samples (no redirect) -> {status}")
    if status == 302:
        print("[auth]   OK: anonymous /samples redirects to login (gate working)")
    elif status == 200 and 'id="samplesTable"' in body:
        print("[auth]   OK: /samples serves the samples table")
    else:
        print("[auth]   FAIL: unexpected status/body for /samples")
        failures.append("auth-gate")

    # 3. Login page renders
    status, body = fetch(f"{base}/login")
    print(f"[page]   GET {base}/login -> {status}")
    if status == 200 and 'name="password"' in body:
        print("[page]   OK: login form reachable")
    else:
        print("[page]   FAIL: login form not found")
        failures.append("login-page")

    if failures:
        print(f"UNHEALTHY: {', '.join(failures)}")
        return 1
    print("HEALTHY")
    return 0


if __name__ == "__main__":
    sys.exit(main())
