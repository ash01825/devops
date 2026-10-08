#!/usr/bin/env python3
"""Health check for the Water Quality Sampling Portal.

Checks:
  1. Spring Boot Actuator health endpoint (expects {"status":"UP"}).
  2. Portal login/samples page reachable (expects HTTP 200).

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


def fetch(url: str, timeout: int = 10):
    """GET url, return (status_code, body_text). Never raises."""
    try:
        with urllib.request.urlopen(url, timeout=timeout) as resp:
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

    # 2. Samples/login page reachable
    status, body = fetch(f"{base}/samples")
    print(f"[page]   GET {base}/samples -> {status}")
    if status == 200:
        print("[page]   OK: /samples reachable")
    else:
        # Fall back to root in case routing differs; still require one 200.
        status2, _ = fetch(f"{base}/")
        print(f"[page]   GET {base}/ -> {status2}")
        if status2 == 200:
            print("[page]   OK: root reachable (samples route may need auth)")
        else:
            print("[page]   FAIL: neither /samples nor / returned 200")
            failures.append("page")

    if failures:
        print(f"UNHEALTHY: {', '.join(failures)}")
        return 1
    print("HEALTHY")
    return 0


if __name__ == "__main__":
    sys.exit(main())
