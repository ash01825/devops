# Ansible — Water Quality Sampling Portal

Local staging deploy via `playbook.yml`, rollback via `rollback.yml`.
Inventory: `inventory.ini` (`localhost`, `ansible_connection=local`).

The `os` conda env holds the helper toolchain; activate it first:

```bash
conda activate os
# or one-shot: conda run -n os <command>
```

## 1. Prerequisites

| Category | Requirement | Check / setup |
|---|---|---|
| Packages | Python 3.10+, ansible-core 2.15+, curl, Docker 24+ | `conda run -n os ansible --version`; `brew install curl docker` |
| Collections | `community.docker`, `ansible.posix` | `ansible-galaxy collection install -r ansible/requirements.yml` |
| Users | Current local user (no new users for local staging) | `whoami` (playbook records it) |
| Folders | `/opt/wq-portal` (deploy), `./postgres_data` (db volume helper) | Created by playbook (`state: directory`) |
| Files | `docker/docker-compose.yml` (source), `/opt/wq-portal/docker-compose.yml` (deployed copy) | Playbook `copy` task ships it |
| Ports | `8080` (app), `5432` (postgres) free | `lsof -i :8080 -i :5432` |
| Services | Docker daemon running | `docker info` |

Install collections once:

```bash
conda activate os
ansible-galaxy collection install -r ansible/requirements.yml
```

## 2. First run (expect changes)

```bash
conda activate os
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml
```

Example output (abridged):

```text
PLAY [Deploy Water Quality Sampling Portal stack] ****
TASK [Ensure deploy directory exists] ****
changed: [localhost]
TASK [Start portal stack with compose v2] ****
changed: [localhost]
TASK [Health-check actuator endpoint (expect HTTP 200)] ****
ok: [localhost]
PLAY RECAP ****
localhost: ok=12 changed=5 unreachable=0 failed=0 skipped=0 rescued=0 ignored=0
```

## 3. Second run — idempotent (expect `changed=0`)

```bash
conda run -n os ansible-playbook -i ansible/inventory.ini ansible/playbook.yml
```

```text
PLAY RECAP ****
localhost: ok=12 changed=0 unreachable=0 failed=0 skipped=0 rescued=0 ignored=0
```

Re-running changes nothing: folders use `state: directory`, images use
`pull: missing`, compose uses `state: present`, and `uri`/`wait_for` only read.

## 4. Health check

```bash
curl -f http://localhost:8080/water-quality-portal/actuator/health
conda run -n os python scripts/health_check.py
```

## 5. Rollback demo

```bash
# Deploy a new version, then roll back to the last stable tag:
conda run -n os ansible-playbook -i ansible/inventory.ini ansible/playbook.yml -e app_version=1.0.1
conda run -n os ansible-playbook -i ansible/inventory.ini ansible/rollback.yml -e prev_version=1.0.0
curl -f http://localhost:8080/water-quality-portal/actuator/health
```

The rollback play stops the current app container, starts
`water-quality-portal:<prev_version>`, waits for port 8080, and fails unless
`/actuator/health` returns HTTP 200.

## 6. Useful extras

```bash
conda run -n os ansible-playbook -i ansible/inventory.ini ansible/playbook.yml --check   # dry run
conda run -n os ansible-playbook -i ansible/inventory.ini ansible/playbook.yml --syntax-check
```
