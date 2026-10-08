# Week 14 — Provisioning Reliability

## Idempotency (second run `changed=0`)

Re-running the deploy playbook changes nothing:

```bash
conda run -n os ansible-playbook -i ansible/inventory.ini ansible/playbook.yml
# PLAY RECAP: localhost: ok=12 changed=0 unreachable=0 failed=0
```

Why it holds: folders use `file: state=directory`, db pull is idempotent,
`docker_compose_v2` uses `state=present, pull=missing`, and `wait_for`/`uri`/`command`
checks are read-only (`changed_when: false`). Only a compose-file change fires the
`Restart app stack` handler. Dry-run anytime with `--check`.

## Health check

Actuator (expects `{"status":"UP"}`, HTTP 200):

```bash
curl -f http://localhost:8080/water-quality-portal/actuator/health
```

Full check (actuator + page reachability, stdlib only, exit 0 = healthy):

```bash
conda run -n os python scripts/health_check.py
conda run -n os python scripts/health_check.py --base-url http://localhost:8080/water-quality-portal
```

The playbook's `uri` task gates the deploy on the same endpoint (12 retries × 10 s).

## rollback.yml (`prev_version` variable)

```bash
conda run -n os ansible-playbook -i ansible/inventory.ini ansible/playbook.yml -e app_version=1.0.1
conda run -n os ansible-playbook -i ansible/inventory.ini ansible/rollback.yml -e prev_version=1.0.0
curl -f http://localhost:8080/water-quality-portal/actuator/health
```

Rollback stops `wq-portal-app`, starts `water-quality-portal:<prev_version>`
(prod datasource env, compose network), waits for 8080, and **fails unless**
`/actuator/health` returns 200. Default `prev_version: "1.0.0"`; always pass `-e`.

## Recovery steps (unhealthy after deploy/rollback)

1. `conda run -n os python scripts/health_check.py` — see which probe failed.
2. `docker compose -f docker/docker-compose.yml ps; docker logs wq-portal-app` (or
   `docker logs wq-portal-db` if `pg_isready` fails).
3. Port clash? `lsof -i :8080 -i :5432`, stop the holder.
4. DB not ready? Confirm `db` is `healthy` — app `depends_on: service_healthy`.
5. Still red? Roll back to last known-good tag (above) and verify health 200.
